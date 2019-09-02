package com.mw.beam.beamwallet.core

import android.annotation.SuppressLint
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class AppModel {
    private var contacts = mutableListOf<WalletAddress>()
    private var addresses = mutableListOf<WalletAddress>()
    private var transactions = mutableListOf<TxDescription>()
    private var utxos = mutableListOf<Utxo>()
    private lateinit var walletStatus:WalletStatus

    private var isSubscribe = false

    var subOnTransactionsChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnUtxosChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnStatusChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()
    var subOnAddressesChanged: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()

    companion object {
        private var INSTANCE: AppModel? = null

        val instance: AppModel
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppModel()
                }

                return INSTANCE!!
            }
    }

    //MARK: -Status

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

    fun getTransactionsByAddress(id: String?) : List<TxDescription> {
        var result = mutableListOf<TxDescription>()

        transactions.forEach {
            if (it.myId == id || it.peerId == id) {
                result.add(it)
            }
        }

        return result
    }

    fun getTransactions() : List<TxDescription> {
        return transactions
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

    @SuppressLint("CheckResult")
    fun subscribeToUpdates() {
        if (!isSubscribe)
        {
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
                subOnAddressesChanged.onNext(0)
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

                subOnAddressesChanged.onNext(0)
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

            App.wallet?.getUtxosStatus()
        }
    }
}