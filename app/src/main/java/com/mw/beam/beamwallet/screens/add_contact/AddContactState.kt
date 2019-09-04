package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag

class AddContactState {
    var tags: List<Tag> = listOf()

    var addresses = HashMap<String, WalletAddress>()

    fun updateAddresses(addresses: List<WalletAddress>?){
        addresses?.forEach {
            this.addresses[it.walletID] = it
        }
    }

    fun getAddresses() = addresses.values.toList()


    fun deleteAddresses(addresses: List<WalletAddress>?) {
        addresses?.forEach {
            this.addresses.remove(it.walletID)
        }
    }
}