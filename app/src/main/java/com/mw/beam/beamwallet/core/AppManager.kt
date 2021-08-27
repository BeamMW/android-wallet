package com.mw.beam.beamwallet.core

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO
import com.mw.beam.beamwallet.core.entities.dto.WalletStatusDTO
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.core.utils.CalendarUtils.calendarFromTimestamp
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.io.File
import java.util.*


class AppManager {
    var wallet: Wallet? = null

    private var handler:android.os.Handler? = null
    private var notificationTimer:Timer? = null

    private var contacts = mutableListOf<WalletAddress>()
    private var addresses = mutableListOf<WalletAddress>()
    private var transactions = mutableListOf<TxDescription>()
    private var utxos = mutableListOf<Utxo>()
    private var shieldedUtxos = mutableListOf<Utxo>()
    private var notifications = mutableListOf<Notification>()
    private var sentNotifications = mutableListOf<String>()
    var ignoreNotifications = mutableListOf<String>()
    var syncProgressData = OnSyncProgressData(1, 1, 0)

    var lastSendingAddress = ""

    private var walletStatus:WalletStatus =
            WalletStatus(WalletStatusDTO(0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0,
                    SystemStateDTO("", 0)))

    private var networkStatus = NetworkStatus.OFFLINE
    private var isSubscribe = false
    var isConnecting = false

    var subOnTransactionsChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnUtxosChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnStatusChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnAddressesChanged: Subject<Boolean?> = PublishSubject.create<Boolean?>().toSerialized()
    var subOnNetworkStatusChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnConnectingChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnFaucedGenerated: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnNotificationsChanged: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnCurrenciesChanged: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnOnNetworkStartReconnecting: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnGetOfflinePayments: Subject<Int?> = PublishSubject.create<Int?>().toSerialized()
    var subOnBeamGameGenerated: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnPublicAddress: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnMaxPrivacyAddress: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnExportToCSV: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnAddressCreated: Subject<WalletAddress> = PublishSubject.create<WalletAddress>().toSerialized()

    private var newAddressSubscription: Disposable? = null

    var isRestored = false

    private var reconnectAttempts = 0
    private var reconnectNodes = mutableListOf<String>()

    companion object {
        private var INSTANCE: AppManager? = null

        val instance: AppManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppManager()
                }

                return INSTANCE!!
            }
    }


    private val gson = Gson()
    private fun getIgnoredContacts(): List<String>  {
        val json = PreferencesManager.getString(PreferencesManager.IGNORE_CONTACTS)

        if (json.isNullOrBlank()) {
            return arrayListOf<String>()
        }

        val token: TypeToken<List<String>> = object : TypeToken<List<String>>() {}
        return gson.fromJson(json, token.type) as List<String>
    }

    fun setIgnoreAddress(id: String?) {
        if (!id.isNullOrEmpty()) {
            val strings = mutableListOf<String>()
            strings.addAll(getIgnoredContacts())
            if (!strings.contains(id)) {
                strings.add(id)
            }
            PreferencesManager.putString(PreferencesManager.IGNORE_CONTACTS, gson.toJson(strings))
        }
    }

    fun isMaxPrivacyEnabled(): Boolean {
       // return true
        val protocolEnabled = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false);
        return wallet?.isConnectionTrusted() == true || protocolEnabled
    }

    fun isSynced(): Boolean {
        return wallet?.isSynced() == true
    }

    fun removeIgnoredAddress(id: String) {
        if (id.isNotEmpty()) {
            val strings = mutableListOf<String>()
            strings.addAll(getIgnoredContacts())
            if (strings.contains(id)) {
                strings.remove(id)
                PreferencesManager.putString(PreferencesManager.IGNORE_CONTACTS, gson.toJson(strings))
            }
        }
    }

    fun reconnect(): Boolean {
        val random = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true);

        if (random) {
            reconnectAttempts += 1
            reconnectNodes.add(AppConfig.NODE_ADDRESS);

            val node = chooseRandomNodeWithoutNodes()

            if(node.isNotEmpty()) {
                AppConfig.NODE_ADDRESS = node

                subOnOnNetworkStartReconnecting.onNext(0)

                instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)

                return true
            }
        }

        return false
    }

    private fun isOwnNode(): Boolean {
        return wallet?.isConnectionTrusted() == true
    }

    private fun chooseRandomNodeWithoutNodes(): String {
        val peers = Api.getDefaultPeers()

        val array = mutableListOf<String>()

        peers.forEach { random ->
            if(!random.contains("shanghai")) {
                var found = false

                array.forEach { node ->
                    if(random == node) {
                        found = true;
                    }
                }

                if(!found) {
                    array.add(random)
                }
            }
        }

        if(array.count() > 0 ) {
            return array[0]
        }

        return ""
    }

    fun reload() {
        wallet?.getWalletStatus()
        wallet?.getAllUtxosStatus()
        wallet?.getAddresses(true)
        wallet?.getAddresses(false)
        wallet?.getTransactions()
        wallet?.getNotifications()
    }

    fun removeOldValues() {
        sentNotifications.clear()
        contacts.clear()
        addresses.clear()
        transactions.clear()
        utxos.clear()
        shieldedUtxos.clear()
        notifications.clear()
        AssetManager.instance.clear()
    }

    fun removeWallet() {
        isSubscribe = false
        isRestored = false

        Api.closeWallet()

        wallet = null

        sentNotifications.clear()
        contacts.clear()
        addresses.clear()
        transactions.clear()
        notifications.clear()
        utxos.clear()
        shieldedUtxos.clear()

        PreferencesManager.clear()
        AssetManager.instance.clear()

        val db = File(AppConfig.DB_PATH, AppConfig.DB_FILE_NAME)

        if (db.exists()) {
            db.delete()
        }

        val fr = File(AppConfig.DB_PATH, AppConfig.NODE_JOURNAL_FILE_NAME)
        if (fr.exists()) {
            fr.delete()
        }

        val directory = File(AppConfig.LOG_PATH)
        val logs = directory.listFiles()

        if (logs!=null) {
            logs.sortBy {
                it.lastModified()
            }

            for (i in logs.indices) {
                if (i > 0) {
                    if (logs[i].exists()) {
                        logs[i].delete()
                    }
                }
            }
        }

        PreferencesManager.putString(PreferencesManager.KEY_TRANSACTIONS, "")
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false);
        PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, "")
        AppConfig.NODE_ADDRESS = randomNode()
    }

    fun randomNode(): String {
        val nodes = Api.getDefaultPeers();
        val result = mutableListOf<String>();
        nodes.forEach {
            if(!it.contains("shanghai")) {
                result.add(it)
            }
        }
        return result.random()
    }

    fun importData(data: String) {
        wallet?.importDataFromJson(data)
    }

    //MARK: -Status

    fun getNetworkStatus():NetworkStatus {
        return networkStatus
    }

    fun getStatus():WalletStatus {
        return walletStatus
    }

    //MARK: - UTXOS

    fun getTransactionsByUTXO(utxo: Utxo?) : List<TxDescription> {
        val result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.id == utxo?.spentTxId)
            {
                result.add(it)
            }
            else if (it.id == utxo?.createTxId)
            {
                result.add(it)
            }
        }

        return result
    }

    fun getUtxoByID(id: String?): Utxo? {
        getUtxos().forEach {
            if (it.stringId == id)
            {
                return it
            }
        }

        return null
    }

    fun getUtxos() : List<Utxo> {
        val result = mutableListOf<Utxo>()
        result.addAll(utxos)
        result.addAll(shieldedUtxos)
        return result
    }

    //MARK: - Addresses
    fun isToken(address: String): Boolean {
        return wallet!!.isToken(address)
    }

    fun isValidAddress(address: String): Boolean {
        if(address.isEmpty()) {
            return false
        }
        return wallet!!.isAddress(address) || wallet!!.isToken(address)
    }

    fun getAllAddresses() : List<WalletAddress> {
        var result = mutableListOf<WalletAddress>()
        result.addAll(contacts)
        result.addAll(addresses)
        return result
    }

    fun getContacts() : List<WalletAddress> {
        return contacts.map { it }.toList()
    }

    fun getMyAddresses() : List<WalletAddress> {
        return addresses.map { it }.toList()
    }

    fun isMyAddress(address: String): Boolean {
        addresses.forEach {
            if (it.id == address) {
                return true
            }
        }
        return false
    }

    fun getAddressByName(name: String?) : WalletAddress? {
        contacts.forEach {
            if (it.label == name)
            {
                return it
            }
        }

        addresses.forEach {
            if (it.label == name)
            {
                return it
            }
        }

        return null
    }

    fun getAddress(id: String?) : WalletAddress? {
        contacts.forEach {
            if (it.id == id || it.getOriginalId == id)
            {
                return it
            }
        }

        addresses.forEach {
            if (it.id == id || it.getOriginalId == id)
            {
                return it
            }
        }

        return null
    }

    private fun deleteAddresses(deleted: List<WalletAddress>) {
        deleted.forEach { item2 ->
            if (item2.isContact)  {
                contacts.removeAll { item1 ->
                    item1.id == item2.id
                }
            }
            else{
                addresses.removeAll { item1 ->
                    item1.id == item2.id
                }
            }
        }
    }

    private fun restoreAddresses(deleted: List<WalletAddress>) {
        deleted.forEach {
            if (it.isContact) {
                contacts.removeAll { item1 ->
                    item1.id == it.id
                }
                contacts.add(it)
            }
            else{
                addresses.removeAll { item1 ->
                    item1.id == it.id
                }
                addresses.add(it)
            }
        }
    }

    //MARK: - Transactions

    fun hasActiveTransactions() :Boolean {
        transactions.forEach {
            if(it.status == TxStatus.Registered || it.status == TxStatus.InProgress ||
                    it.status == TxStatus.Pending) {
                return true
            }
        }

        return false
    }

    fun getTransactionById(id: String?) : TxDescription? {
        transactions.forEach {
            if (it.id == id) {
                return it
            }
        }

        return null
    }

    fun getTransactionsByAddress(id: String?) : List<TxDescription> {
        val result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.myId == id || it.peerId == id || it.token == id) {
                result.add(it)
            }
        }

        return result
    }

    fun getAllTransactionsByAddress(id: String?) : List<TxDescription> {
        var result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.myId == id || it.peerId == id || it.token == id) {
                result.add(it)
            }
        }

        TrashManager.getAllData().transactions.forEach() {
            if (it.myId == id || it.peerId == id  || it.token == id) {
                result.add(it)
            }
        }

        return result
    }

    fun getTransactions() : List<TxDescription> {
        return transactions.map { it }.toList()
    }

    fun getUTXOByTransaction(tx: TxDescription) : List<Utxo> {
        val result = mutableListOf<Utxo>()

        getUtxos().forEach {
            if (it.createTxId == tx.id)
            {
                result.add(it)
            }
            else if (it.spentTxId == tx.id)
            {
                result.add(it)
            }
        }

        return result
    }

    fun getTransaction(id: String) : TxDescription? {
        transactions.forEach {
            if (it.id == id)
            {
                return it
            }
        }

        return null
    }



    private fun restoreTransactions(deleted: List<TxDescription>) {
        deleted.forEach {
            transactions.removeAll { item1 ->
                item1.id == it.id
            }
            transactions.add(it)
        }
    }

    private fun updateTransactions(updated: List<TxDescription>) {
        updated.forEach { item2 ->
            val index = transactions.indexOfFirst {
                it.id == item2.id
            }
            if (index != -1) {
                transactions[index] = item2
            }
        }
    }

    private fun deleteTransactions(deleted: List<TxDescription>) {
        deleted.forEach { item2 ->
            transactions.removeAll { item1 ->
                item1.id == item2.id
            }
        }
    }

    private fun deleteUtxo(deleted: List<Utxo>) {
        deleted.forEach { item2 ->
            utxos.removeAll { item1 ->
                item1.id == item2.id
            }
        }

        deleted.forEach { item2 ->
            shieldedUtxos.removeAll { item1 ->
                item1.id == item2.id
            }
        }
    }

    private fun updateUtxo(updated: List<Utxo>) {
        updated.forEach { item2 ->
            val index = utxos.indexOfFirst {
                it.id == item2.id
            }
            if (index != -1) {
                utxos[index] = item2
            }
        }

        updated.forEach { item2 ->
            val index = shieldedUtxos.indexOfFirst {
                it.id == item2.id
            }
            if (index != -1) {
                shieldedUtxos[index] = item2
            }
        }
    }

    //MARK: - Notifications

    fun getUnreadNotificationsCount(): Int {
        var count = 0
        notifications.forEach {
            if(!it.isRead) {
                count += 1
            }
        }
        return count;
    }

    fun getUnsentNotificationsCount(): Int {
        var count = 0
        notifications.forEach {
            if(!it.isSent && !it.isRead) {
                count += 1
            }
        }
        return count;
    }

    fun getUnsentNotification(): Notification? {
        notifications.forEach {
            if (!it.isSent && !it.isRead) {
                return  it
            }
        }
        return null
    }

    fun getLastVersionNotification(): Notification? {
        notifications.forEach {
            if (it.type == NotificationType.Version && !it.isRead) {
                return  it
            }
        }
        return null
    }

    fun allUnsentIsAddresses(): Boolean {
        notifications.forEach {
            if(it.type != NotificationType.Address && !it.isSent && !it.isRead) {
                return false
            }
        }
        return true
    }

    fun readNotification(id: String) {
        wallet?.markNotificationAsRead(id)
    }

    fun readAllNotification() {
        val list = notifications.toMutableList()

        list.forEach {
            if (!it.isRead) {
                readNotification(it.id)
            }
        }
    }

    fun readNotificationByObject(id: String) {
        notifications.forEach {
            if(it.objId == id) {
                readNotification(it.id)
                return
            }
        }
    }

    fun deleteNotification(id: String) {
        wallet?.deleteNotification(id)
    }

    fun deleteAllNotificationByObject(id: String) {
        notifications.forEach {
            if (it.objId == id) {
                deleteNotification(it.id)
            }
            return;
        }
    }

    fun deleteAllNotificationTransactions() {
        notifications.forEach {
            if (it.type == NotificationType.Transaction) {
                deleteNotification(it.id)
            }
        }
    }

    fun deleteAllNotifications(list: List<String>) {
        list.forEach {
            deleteNotification(it)
            notifications.removeAll { item ->
                item.id == it
            }
        }

        (AppActivity.self)?.reloadNotifications()

        subOnNotificationsChanged.onNext(0)
    }

    fun deleteAllNotifications() {
        notifications.toMutableList().forEach {
           deleteNotification(it.id)
        }
        notifications.clear()

        (AppActivity.self)?.reloadNotifications()

        subOnNotificationsChanged.onNext(0)
    }

    fun sendNotifications() {
        notifications.forEach {
            if(!it.isSent) {
                it.isSent = true
                sentNotifications.add(it.id)
            }
        }
    }

    fun getNotifications() : List<Notification> {
        var results = notifications.map { it }.toList()
        var lists = mutableListOf<Notification>()

        results.forEach {
            if (it.type == NotificationType.Transaction) {
                val tr = getTransactionById(it.objId)
                if (tr != null) {
                    lists.add(it)
                }
            }
            else {
                lists.add(it)
            }
        }

        lists.sortBy {
            it.createdTime
        }

        return lists
    }

    //MARK: - Updates

    fun onChangeNodeAddress() {
        isConnecting = true

        subOnConnectingChanged.onNext(0)
    }

    fun requestUTXO() {
        wallet?.getAllUtxosStatus()
    }

    fun unSubscribeToUpdates() {
        if (isSubscribe) {
            Log.e("UN_SUBSCRIBE", "UN_SUBSCRIBE")

            isSubscribe = false
        }
    }

    fun canSendToMaxPrivacy(address: String): Boolean {
        return true
    }

    var ignoreNetworkStatus = false
    fun checkConnection(resume: Boolean) {
        ignoreNetworkStatus = !resume

        if (networkStatus == NetworkStatus.OFFLINE && resume) {
            val reconnect = reconnect()
            if (!reconnect) {
                networkStatus = NetworkStatus.OFFLINE
                subOnNetworkStatusChanged.onNext(0)
            }
        }
    }

    fun updateAllData() {
        wallet?.getWalletStatus()
        wallet?.getAllUtxosStatus()
        wallet?.getAddresses(true)
        wallet?.getAddresses(false)
        wallet?.getExchangeRates()
    }

    fun createAddressForBeamGame() {
        val address = getAddressByName("Beam runner")

        if (address == null || address.isExpired)
        {
            newAddressSubscription = WalletListener.subOnGeneratedNewAddress.subscribe(){
                newAddressSubscription?.dispose()
                newAddressSubscription = null

                it.label = "Beam runner"
                it.duration = 0L
                wallet?.saveAddress(it.toDTO(), true)

                subOnBeamGameGenerated.onNext(it.id)

                subscribeToNewAddress()
            }

            wallet?.generateNewAddress()
        }
        else{
            subOnBeamGameGenerated.onNext(address.id)
        }
    }

    fun createAddressForFaucet() {
        val address = getAddressByName("Beam community faucet")

        if (address == null || address.isExpired)
        {
            newAddressSubscription = WalletListener.subOnGeneratedNewAddress.subscribe(){
                newAddressSubscription?.dispose()
                newAddressSubscription = null

                it.label = "Beam community faucet"
                it.duration = 0L
                wallet?.saveAddress(it.toDTO(), true)

                subOnFaucedGenerated.onNext(it.id)

                subscribeToNewAddress()
            }

            wallet?.generateNewAddress()
        }
        else{
            subOnFaucedGenerated.onNext(address.id)
        }
    }

    fun getPublicAddress() {
        wallet?.getPublicAddress()
    }

    fun setNetworkStatus(it: Boolean) {
        networkStatus = if (it) NetworkStatus.ONLINE else NetworkStatus.OFFLINE

        if (it)
        {
            isOwnNode()
        }

        if (isConnecting)
        {
            val delay = if (it) 1000L else 3000L
            handler?.removeCallbacksAndMessages(null);
            handler = null

            handler = android.os.Handler()
            handler?.postDelayed({
                isConnecting = false
                subOnNetworkStatusChanged.onNext(0)
            }, delay)

            if (!it) {
                val reconnect = reconnect()
                if (!reconnect) {
                    networkStatus = NetworkStatus.OFFLINE
                    subOnNetworkStatusChanged.onNext(0)
                }
            }
        }
        else if (!it && !isConnecting) {
            isConnecting = false

            subOnNetworkStatusChanged.onNext(0)

            val reconnect = reconnect()
            if (!reconnect) {
                networkStatus = NetworkStatus.OFFLINE
                subOnNetworkStatusChanged.onNext(0)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun subscribeToUpdates() {
        if (!isSubscribe)
        {
            val json = PreferencesManager.getString(PreferencesManager.KEY_TRANSACTIONS)

            if (!json.isNullOrBlank()) {
                val g = Gson()
                val token: TypeToken<List<TxDescription>> = object : TypeToken<List<TxDescription>>() {}
                val a = g.fromJson(json, token.type) as List<TxDescription>
                transactions.clear()
                transactions.addAll(a)
            }

            Log.e("SUBSCRIBE", "SUBSCRIBE")

            isSubscribe = true

            TrashManager.subOnTrashChanged.subscribe(){ item ->

                if (item.type == TrashManager.ActionType.Added)
                {
                    deleteAddresses(item.data.addresses)
                    deleteTransactions(item.data.transactions)
                }
                else if (item.type == TrashManager.ActionType.Restored)
                {
                    restoreAddresses(item.data.addresses)
                    restoreTransactions(item.data.transactions)
                }


                subOnTransactionsChanged.onNext(0)
                subOnAddressesChanged.onNext(true)
            }

            WalletListener.subOnAddresses.subscribe(){
                if (it.own) {
                    addresses.clear()
                    if (it.addresses!=null) addresses.addAll(it.addresses)

                    deleteAddresses(TrashManager.getAllData().addresses)
                }
                else if (!it.own) {
                    contacts.clear()
                    if (it.addresses!=null) {
                        val ignored = getIgnoredContacts()
                        it.addresses.forEach {address->
                            if(!ignored.contains(address.address) && !ignored.contains(address.id)) {
                                contacts.add(address)
                            }
                        }
                    }

                    deleteAddresses(TrashManager.getAllData().addresses)
                }

                subOnAddressesChanged.onNext(it.own)
            }

            WalletListener.subOnGetOfflinePaymentCount.subscribe() {
                subOnGetOfflinePayments.onNext(it)
            }

            WalletListener.obsOnTxStatus.subscribe(){

                if (it.action == ChangeAction.REMOVED && it.tx != null) {
                    deleteTransactions(it.tx)
                }
                else if (it.action == ChangeAction.ADDED && it.tx != null) {
                    transactions.addAll(it.tx)
                }
                else if (it.action == ChangeAction.RESET && it.tx != null) {
                    transactions.clear()
                    transactions.addAll(it.tx)
                }
                else if (it.action == ChangeAction.UPDATED && it.tx != null) {
                    updateTransactions(it.tx)
                }

                deleteTransactions(TrashManager.getAllData().transactions)

                transactions.removeAll { item ->
                    calendarFromTimestamp(item.createTime).get(Calendar.YEAR) == 1970
                }

                val g = Gson()
                val jsonString = g.toJson(transactions)
                PreferencesManager.putString(PreferencesManager.KEY_TRANSACTIONS, jsonString)

                subOnTransactionsChanged.onNext(0)
            }

            WalletListener.subOnAllUtxoChanged.subscribe(){
                utxos.clear()
                utxos.addAll(it)
                subOnUtxosChanged.onNext(0)
            }

            WalletListener.obsOnShieldedUtxos.subscribe {
                if (it.utxo != null) {
                    it.utxo.forEach { u->
                        u.keyType = UtxoKeyType.Shielded
                    }
                }
                if (it.action == ChangeAction.REMOVED && it.utxo != null) {
                    deleteUtxo(it.utxo)
                }
                else if (it.action == ChangeAction.ADDED && it.utxo != null) {
                    shieldedUtxos.addAll(it.utxo)
                }
                else if (it.action == ChangeAction.RESET && it.utxo != null) {
                    shieldedUtxos.clear()
                    shieldedUtxos.addAll(it.utxo)
                }
                else if (it.action == ChangeAction.UPDATED && it.utxo != null) {
                    updateUtxo(it.utxo)
                }

                subOnUtxosChanged.onNext(0)
            }

            WalletListener.obsOnUtxos.subscribe{
                if (it.action == ChangeAction.REMOVED && it.utxo != null) {
                    deleteUtxo(it.utxo)
                }
                else if (it.action == ChangeAction.ADDED && it.utxo != null) {
                    utxos.addAll(it.utxo)
                }
                else if (it.action == ChangeAction.RESET && it.utxo != null) {
                    utxos.clear()
                    utxos.addAll(it.utxo)
                }
                else if (it.action == ChangeAction.UPDATED && it.utxo != null) {
                    updateUtxo(it.utxo)
                }

                subOnUtxosChanged.onNext(0)
            }

            WalletListener.subOnAddressesChanged.subscribe{ items ->
                if (items.action == ChangeAction.REMOVED && items.addresses != null) {
                    deleteAddresses(items.addresses)
                }
                else if (items.action == ChangeAction.ADDED && items.addresses != null)
                    items.addresses.forEach { item->
                    if (item.isContact) {
                        val index1 = contacts.indexOfFirst {
                            it.id == item.id
                        }
                        if (index1 != -1) {
                            contacts[index1] = item
                        }
                        else{
                            contacts.add(item)
                        }
                    } else{
                        val index2 = addresses.indexOfFirst {
                            it.id == item.id
                        }
                        if (index2 != -1) {
                            addresses[index2] = item
                        }
                        else{
                            addresses.add(item)
                        }
                    }
                }
                else if (items.action == ChangeAction.RESET && items.addresses != null) {
                    wallet?.getAddresses(true)
                    wallet?.getAddresses(false)
                }
                else if (items.action == ChangeAction.UPDATED && items.addresses != null) {
                    items.addresses.forEach { item ->
                        val index1 = contacts.indexOfFirst {
                            it.id == item.id
                        }
                        if (index1 != -1) {
                            contacts[index1] = item
                        }

                        val index2 = addresses.indexOfFirst {
                            it.id == item.id
                        }
                        if (index2 != -1) {
                            addresses[index2] = item
                        }
                    }
                }

                subOnAddressesChanged.onNext(true)
            }

            WalletListener.subOnStatus.subscribe {
                walletStatus = it
                subOnStatusChanged.onNext(0)
            }

            WalletListener.subOnAssetInfo.subscribe {
                subOnStatusChanged.onNext(0)
            }

            WalletListener.subOnPublicAddress.subscribe {
                subOnPublicAddress.onNext(it)
            }

            WalletListener.subOnMaxPrivacyAddress.subscribe {
                subOnMaxPrivacyAddress.onNext(it)
            }

            WalletListener.subOnNodeConnectedStatusChanged.subscribe {
                setNetworkStatus(it)
            }

            WalletListener.subOnNodeConnectionFailed.subscribe {
                val reconnect = reconnect()
                if (!reconnect) {
                    networkStatus = NetworkStatus.OFFLINE
                    subOnNetworkStatusChanged.onNext(0)
                }
            }

            WalletListener.subOnSyncProgressUpdated.subscribe {
                syncProgressData = it
                networkStatus = if (it.done == it.total) NetworkStatus.ONLINE else NetworkStatus.UPDATING
                subOnNetworkStatusChanged.onNext(0)
            }

            WalletListener.subOnExchangeRates.subscribe {
               AppActivity.self.runOnUiThread {
                   subOnNetworkStatusChanged.onNext(0)
                   subOnCurrenciesChanged.onNext(0)
                   subOnStatusChanged.onNext(0)
               }
            }

            WalletListener.subNotificationChanged.subscribe {
                if (it.action == ChangeAction.REMOVED) {
                    notifications.removeAll { item ->
                        item.id == it.notification.id
                    }
                }
                else if (it.action == ChangeAction.ADDED || it.action == ChangeAction.RESET) {
                    val walledUpdatesOn = PreferencesManager.getBoolean(PreferencesManager.KEY_WALLET_UPDATES, true);
                    val addressesOn = PreferencesManager.getBoolean(PreferencesManager.KEY_ADDRESS_EXPIRATION, true);
                    val transactionOn = PreferencesManager.getBoolean(PreferencesManager.KEY_TRANSACTIONS_STATUS, true);
                    val newsOn = PreferencesManager.getBoolean(PreferencesManager.KEY_NEWS, true);

                    if(it.notification.type == NotificationType.Version && !walledUpdatesOn) {
                        return@subscribe
                    }
                    else if(it.notification.type == NotificationType.Address && !addressesOn) {
                        return@subscribe
                    }
                    else if(it.notification.type == NotificationType.Transaction && !transactionOn) {
                        return@subscribe
                    }
                    else if(it.notification.type == NotificationType.News && !newsOn) {
                        return@subscribe
                    }

                    it.notification.isSent = sentNotifications.contains(it.notification.id)

                    if(!it.notification.isSent) {
                        it.notification.isSent = ignoreNotifications.contains((it.notification.objId))
                    }

                    val index = notifications.indexOfFirst { item->
                        item.id == it.notification.id
                    }
                    if (index != -1) {
                        notifications[index] = it.notification
                    }
                    else {
                        notifications.add(it.notification)
                    }
                }
                else {
                    val index = notifications.indexOfFirst { item->
                        item.id == it.notification.id
                    }
                    if (index != -1) {
                        notifications[index] = it.notification
                    }
                }

                notificationTimer?.cancel()
                notificationTimer = null
                notificationTimer = Timer()
                notificationTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        (AppActivity.self)?.reloadNotifications()
                        subOnNotificationsChanged.onNext(0)
                    }
                }, 2000)

            }

            WalletListener.suboOExportTxHistoryToCsv.subscribe {
                subOnExportToCSV.onNext(it)
            }

            subscribeToNewAddress()

            val protocolEnabled = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false);

            if(protocolEnabled) {
               wallet?.enableBodyRequests(protocolEnabled)
            }

            wallet?.switchOnOffExchangeRates(true)
            wallet?.switchOnOffNotifications(0, false)
            wallet?.switchOnOffNotifications(1, false)
            wallet?.switchOnOffNotifications(2, true)
            wallet?.switchOnOffNotifications(3, false)
            wallet?.switchOnOffNotifications(4, true)
            wallet?.switchOnOffNotifications(5, true)

            wallet?.getWalletStatus()
            wallet?.getAllUtxosStatus()
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
            wallet?.getTransactions()
            wallet?.getExchangeRates()
            wallet?.getNotifications()
            wallet?.getMaxPrivacyLockTimeLimitHoursAsync()
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToNewAddress() {
        WalletListener.subOnGeneratedNewAddress.subscribe {
            subOnAddressCreated.onNext(it)
        }
    }
}