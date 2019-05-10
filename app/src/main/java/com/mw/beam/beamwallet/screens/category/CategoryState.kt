package com.mw.beam.beamwallet.screens.category

import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category

class CategoryState {
    var category: Category? = null
    private val hashMapAddresses: HashMap<String, WalletAddress> = HashMap()
    val addresses: List<WalletAddress> = hashMapAddresses.values.toList()

    fun updateAddresses(addresses: List<WalletAddress>) {
        addresses.forEach {
            hashMapAddresses.put(it.walletID, it)
        }
    }
}