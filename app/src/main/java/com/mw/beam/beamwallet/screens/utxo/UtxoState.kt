package com.mw.beam.beamwallet.screens.utxo

import com.mw.beam.beamwallet.core.entities.TxDescription

class UtxoState {
    var privacyMode = false

    val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>? = null) {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }
    }

    fun deleteTransactions(tx: List<TxDescription>?) {
        tx?.forEach { transactions.remove(it.id) }
    }

    fun getTransactions() = transactions.values.sortedByDescending { it.createTime }
}