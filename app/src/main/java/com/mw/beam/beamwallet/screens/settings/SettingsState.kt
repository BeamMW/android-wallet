package com.mw.beam.beamwallet.screens.settings

import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class SettingsState {

    val addresses: List<WalletAddress>
        get() = AppManager.instance.getMyAddresses()

    val contacts: List<WalletAddress>
        get() = AppManager.instance.getContacts()

    val transactions: List<TxDescription>
        get() = AppManager.instance.getTransactions()
}