package com.mw.beam.beamwallet.screens.search_transaction

import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class SearchTransactionState {
    var searchText = ""

    fun getAddresses() = AppManager.instance.getAllAddresses()

    fun getAllTransactions(assetId:Int) = if (assetId == -1) {
        AppManager.instance.getTransactions()
    }
    else {
        AppManager.instance.getTransactions().filter {
            it.assetId == assetId
        }
    }
}