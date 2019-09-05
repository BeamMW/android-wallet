package com.mw.beam.beamwallet.screens.settings

import com.mw.beam.beamwallet.core.AppModel
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class SettingsState {

    val addresses: List<WalletAddress>
        get() = AppModel.instance.getMyAddresses()

    val contacts: List<WalletAddress>
        get() = AppModel.instance.getContacts()

    val transactions: List<TxDescription>
        get() = AppModel.instance.getTransactions()
}