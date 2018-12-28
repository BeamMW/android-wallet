package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.toHex

/**
 * Created by vain onnellinen on 12/28/18.
 */
class UtxoState {
    private val transactions = HashMap<String, TxDescription>()

    fun configTransactions(tx: Array<TxDescription>? = null): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id.toHex()] = transaction
        }

        return transactions.values.sortedByDescending { it.modifyTime }
    }
}
