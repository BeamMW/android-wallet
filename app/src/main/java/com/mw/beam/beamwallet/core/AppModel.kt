package com.mw.beam.beamwallet.core

import android.annotation.SuppressLint
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.Subject

class AppModel {
    private var contacts = mutableListOf<WalletAddress>()
    private var addresses = mutableListOf<WalletAddress>()

    private var isSubscribe = false

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

    @SuppressLint("CheckResult")
    fun subscribeToUpdates() {
        if (!isSubscribe)
        {
            isSubscribe = true

            TrashManager.subOnTrashChanged.subscribe(){ item ->
                when (item.type) {
                    TrashManager.ActionType.Added -> deleteAddresses(item.data.addresses)

                    TrashManager.ActionType.Restored -> restoreAddresses(item.data.addresses)

                    TrashManager.ActionType.Removed -> {
                    }
                }
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
            }
        }
    }


    fun getAddress(id:String) : WalletAddress? {
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

}