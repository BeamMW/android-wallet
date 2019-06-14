package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class ChangeAddressState {
    private val addresses = HashMap<String, WalletAddress>()
    private val transactions = HashMap<String, TxDescription>()
    var viewState = ChangeAddressContract.ViewState.Receive

    fun updateAddresses(walletAddresses: List<WalletAddress>?) {
        walletAddresses?.forEach {
            addresses[it.walletID] = it
        }
    }

    fun updateTransactions(transactions: List<TxDescription>?) {
        transactions?.forEach {
            this.transactions[it.id] = it
        }
    }

    fun getAddresses() = addresses.values.toList()

    fun getTransactions() = transactions.values.toList().sortedBy { it.modifyTime }
}