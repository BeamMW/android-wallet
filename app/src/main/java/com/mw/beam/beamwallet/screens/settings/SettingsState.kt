package com.mw.beam.beamwallet.screens.settings

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class SettingsState {
    var addresses: List<WalletAddress> = listOf()
    var contacts: List<WalletAddress> = listOf()
    val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?) {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }
    }
}