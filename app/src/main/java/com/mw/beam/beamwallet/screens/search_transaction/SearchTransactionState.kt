package com.mw.beam.beamwallet.screens.search_transaction

import com.mw.beam.beamwallet.core.entities.TxDescription

class SearchTransactionState {
    var searchText = ""
    private val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?) {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }
    }

    fun getAllTransactions() = transactions.values.toList()
}