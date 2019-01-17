package com.mw.beam.beamwallet.wallet

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WalletState {
    var walletStatus: WalletStatus? = null

    var shouldExpandAvailable = false
    var shouldExpandInProgress = false

    private val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }

        return transactions.values.sortedByDescending { it.modifyTime }
    }
}
