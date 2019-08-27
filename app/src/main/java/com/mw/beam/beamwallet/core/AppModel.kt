package com.mw.beam.beamwallet.core

import android.annotation.SuppressLint
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
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

    @SuppressLint("CheckResult")
    fun subscribeToUpdates() {
        if (!isSubscribe)
        {
            isSubscribe = true

            WalletListener.subOnAddresses.subscribe(){
                if (it.own && it.addresses!=null) {
                    addresses.clear()
                    addresses.addAll(it.addresses)
                }
                else if (!it.own && it.addresses!=null) {
                    contacts.clear()
                    contacts.addAll(it.addresses)
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