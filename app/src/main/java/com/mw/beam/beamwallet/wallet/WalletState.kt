package com.mw.beam.beamwallet.wallet

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.toHex

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WalletState {
    var maturing: Long = 0L
    var receiving: Long = 0L
    var sending: Long = 0L
    var available: Long = 0L

    var shouldExpandAvailable = false
    var shouldExpandInProgress = false

    private val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: Array<TxDescription>?): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id.toHex()] = transaction
        }

        return transactions.values.sortedByDescending { it.modifyTime }
    }
}
