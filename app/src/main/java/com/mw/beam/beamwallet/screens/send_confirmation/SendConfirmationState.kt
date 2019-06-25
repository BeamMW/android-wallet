package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.core.entities.WalletAddress

class SendConfirmationState {
    var contact: WalletAddress? = null
    val addresses = HashMap<String, WalletAddress>()
    var outgoingAddress: String = ""
    var token: String = ""
    var comment: String? = null
    var amount: Long = 0
    var fee: Long = 0
}