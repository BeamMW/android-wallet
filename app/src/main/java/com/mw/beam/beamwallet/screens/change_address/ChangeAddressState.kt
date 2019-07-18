package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class ChangeAddressState {
    var generatedAddress: WalletAddress? = null
    private val addresses = HashMap<String, WalletAddress>()
    private val transactions = HashMap<String, TxDescription>()
    var scannedAddress : String? = null
    var viewState = ChangeAddressContract.ViewState.Receive

    fun updateAddresses(walletAddresses: List<WalletAddress>?) {
        walletAddresses?.forEach {
            addresses[it.walletID] = it
        }
    }

    fun deleteAddresses(walletAddresses: List<WalletAddress>?) {
        walletAddresses?.forEach { addresses.remove(it.walletID) }
    }

    fun deleteTransactions(transactions: List<TxDescription>?) {
        transactions?.forEach { this.transactions.remove(it.id) }
    }

    fun updateTransactions(transactions: List<TxDescription>?) {
        transactions?.forEach {
            this.transactions[it.id] = it
        }
    }

    fun getAddresses() = ArrayList(addresses.values.toList()).apply {
        generatedAddress?.let { add(0, it) }
    }

    fun getTransactions() = transactions.values.toList().sortedByDescending { it.modifyTime }
}