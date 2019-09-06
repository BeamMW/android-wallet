package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag

class AddContactState {
    var tags: List<Tag> = listOf()

    var addresses = HashMap<String, WalletAddress>()

    fun getAddresses() = addresses.values.toList()
}