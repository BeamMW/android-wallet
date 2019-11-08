package com.mw.beam.beamwallet.core

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.Gson
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import com.mw.beam.beamwallet.core.utils.CalendarUtils.calendarFromTimestamp
import io.reactivex.disposables.Disposable
import java.util.Calendar
import android.os.Handler
import com.mw.beam.beamwallet.core.helpers.*
import java.io.File

class AppManager {
    var wallet: Wallet? = null

    var lastGeneratedAddress:String? = null

    private var handler:android.os.Handler? = null

    private var contacts = mutableListOf<WalletAddress>()
    private var addresses = mutableListOf<WalletAddress>()
    private var transactions = mutableListOf<TxDescription>()
    private var utxos = mutableListOf<Utxo>()
    private lateinit var walletStatus:WalletStatus

    private var networkStatus = NetworkStatus.ONLINE
    private var isSubscribe = false
    var isConnecting = false

    var subOnTransactionsChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnUtxosChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnStatusChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnAddressesChanged: Subject<Boolean?> = PublishSubject.create<Boolean?>().toSerialized()
    var subOnNetworkStatusChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnConnectingChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnFaucedGenerated: Subject<String> = PublishSubject.create<String>().toSerialized()
    private var newAddressSubscription: Disposable? = null

    var isResotred = false

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

    fun removeWallet() {
        isSubscribe = false
        isResotred = false

        Api.closeWallet()

        wallet = null

        contacts.clear()
        addresses.clear()
        transactions.clear()
        utxos.clear()

        PreferencesManager.clear()

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
    }

    fun importData(data:String) {
        val json = Gson()

        val map = json.fromJson(data, HashMap::class.java)
        val tags = map["Categories"]

        if (tags!=null) {
            val tagsString = json.toJson(tags).toString()
            val tagData = json.fromJson(tagsString, Array<Tag>::class.java)
            for (t in tagData) {
                TagHelper.saveTag(t)
            }
        }

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

    fun getTransactionsByUTXO(utxo:Utxo?) : List<TxDescription> {
        var result = mutableListOf<TxDescription>()

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

    fun getUtxoByID(id:String?): Utxo? {
        utxos.forEach {
            if (it.stringId == id)
            {
                return it
            }
        }

        return null
    }

    fun getUtxos() : List<Utxo> {
        return utxos
    }

    //MARK: - Addresses

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

    fun getAddressByName(name:String?) : WalletAddress? {
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

    fun getAddress(id:String?) : WalletAddress? {
        contacts.forEach {
            if (it.walletID == id)
            {
                return it
            }
        }

        addresses.forEach {
            if (it.walletID == id)
            {
                return it
            }
        }

        return null
    }

    private fun deleteAddresses(deleted: List<WalletAddress>) {
        deleted.forEach { item2 ->
            if (item2.isContact)  {
                contacts.removeAll {item1 ->
                    item1.walletID == item2.walletID
                }
            }
            else{
                addresses.removeAll {item1 ->
                    item1.walletID == item2.walletID
                }
            }
        }
    }

    private fun restoreAddresses(deleted: List<WalletAddress>) {
        deleted.forEach {
            if (it.isContact) {
                contacts.removeAll {item1 ->
                    item1.walletID == it.walletID
                }
                contacts.add(it)
            }
            else{
                addresses.removeAll {item1 ->
                    item1.walletID == it.walletID
                }
                addresses.add(it)
            }
        }
    }

    //MARK: - Transactions

    fun getTransactionById(id: String?) : TxDescription? {
        transactions.forEach {
            if (it.id == id) {
                return it
            }
        }

        return null
    }

    fun getTransactionsByAddress(id: String?) : List<TxDescription> {
        var result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.myId == id || it.peerId == id) {
                result.add(it)
            }
        }

        return result
    }

    fun getAllTransactionsByAddress(id: String?) : List<TxDescription> {
        var result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.myId == id || it.peerId == id) {
                result.add(it)
            }
        }

        TrashManager.getAllData().transactions.forEach() {
            if (it.myId == id || it.peerId == id) {
                result.add(it)
            }
        }

        return result
    }

    fun getTransactions() : List<TxDescription> {
        return transactions.map { it }.toList()
    }

    fun getUTXOByTransaction(tx:TxDescription) : List<Utxo> {
        var result = mutableListOf<Utxo>()

        utxos.forEach {
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

    fun getTransaction(id:String) : TxDescription? {
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
            transactions.removeAll {item1 ->
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
            transactions.removeAll {item1 ->
                item1.id == item2.id
            }
        }
    }

    //MARK: - Updates

    fun onChangeNodeAddress() {
        isConnecting = true

        subOnConnectingChanged.onNext(0)
    }

    fun requestUTXO() {
        wallet?.getUtxosStatus()
    }

    fun unSubscribeToUpdates() {
        if (isSubscribe) {
            Log.e("UN_SUBSCRIBE","UN_SUBSCRIBE")

            isSubscribe = false
        }
    }

    fun updateAllData() {
        wallet?.getWalletStatus()
        wallet?.getUtxosStatus()
        wallet?.getAddresses(true)
        wallet?.getAddresses(false)
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

                subOnFaucedGenerated.onNext(it.walletID)
            }

            wallet?.generateNewAddress()
        }
        else{
            subOnFaucedGenerated.onNext(address.walletID)
        }

    }

    @SuppressLint("CheckResult")
    fun subscribeToUpdates() {
        if (!isSubscribe)
        {
            Log.e("SUBSCRIBE","SUBSCRIBE")

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
                    if (it.addresses!=null) contacts.addAll(it.addresses)

                    deleteAddresses(TrashManager.getAllData().addresses)
                }

                subOnAddressesChanged.onNext(it.own)
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

                transactions.removeAll {item ->
                    calendarFromTimestamp(item.createTime).get(Calendar.YEAR) == 1970
                }

                subOnTransactionsChanged.onNext(0)
            }

            WalletListener.subOnAllUtxoChanged.subscribe(){
                utxos.clear()
                utxos.addAll(it)

                subOnUtxosChanged.onNext(0)
            }

            WalletListener.subOnStatus.subscribe(){
                walletStatus = it

                subOnStatusChanged.onNext(0)
            }


            WalletListener.subOnNodeConnectedStatusChanged.subscribe(){
                networkStatus = if (it) NetworkStatus.ONLINE else NetworkStatus.OFFLINE

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
                }
                else {
                    isConnecting = false

                    subOnNetworkStatusChanged.onNext(0)
                }
            }

            WalletListener.subOnNodeConnectionFailed.subscribe(){
                networkStatus = NetworkStatus.OFFLINE
                subOnNetworkStatusChanged.onNext(0)
            }

            WalletListener.subOnSyncProgressUpdated.subscribe(){
                networkStatus = if (it.done == it.total) NetworkStatus.ONLINE else NetworkStatus.UPDATING
                subOnNetworkStatusChanged.onNext(0)
            }

            wallet?.getWalletStatus()
            wallet?.getUtxosStatus()
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
            wallet?.getTransactions()

            Handler().postDelayed({
                TagHelper.fixLegacyFormat()
            }, 2000)
        }
    }
}