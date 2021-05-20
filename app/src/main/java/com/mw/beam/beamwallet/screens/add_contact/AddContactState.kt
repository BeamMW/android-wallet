package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class AddContactState {
    var addresses = HashMap<String, WalletAddress>()

    fun getAddresses() = addresses.values.toList()
}