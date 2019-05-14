package com.mw.beam.beamwallet.screens.category

import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category

class CategoryState {
    var category: Category? = null
    private val hashMapAddresses: HashMap<String, WalletAddress> = HashMap()
    val addresses: List<WalletAddress>
        get() =  hashMapAddresses.values.toList()

    fun addAddresses(addresses: List<WalletAddress>) {
        addresses.forEach {
            hashMapAddresses[it.walletID] = it
        }
    }

    fun setAddresses(addresses: List<WalletAddress>) {
        hashMapAddresses.clear()
        addresses.forEach {
            hashMapAddresses[it.walletID] = it
        }
    }
}