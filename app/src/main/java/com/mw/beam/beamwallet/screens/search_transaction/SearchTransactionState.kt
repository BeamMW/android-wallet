package com.mw.beam.beamwallet.screens.search_transaction

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class SearchTransactionState {
    var searchText = ""
    var addresses = HashMap<String, WalletAddress>()
    private val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?) {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }
    }

    fun getAllTransactions() = transactions.values.toList()

    fun updateAddresses(addresses: List<WalletAddress>?){
        addresses?.forEach {
            this.addresses[it.walletID] = it
        }
    }
}