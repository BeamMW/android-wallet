package com.mw.beam.beamwallet.screens.transaction_details

import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription

class TransactionDetailsState {
    private val transactions = HashMap<String, TxDescription>()
    var txDescription: TxDescription? = null
    var paymentProof: PaymentProof? = null

    fun configTransactions(tx: List<TxDescription>? = null): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }

        return transactions.values.toList()
    }
}