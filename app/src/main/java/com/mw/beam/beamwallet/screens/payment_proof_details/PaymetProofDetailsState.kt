package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription

class PaymetProofDetailsState {
    var txId: String? = null
    var txDescription: TxDescription? = null
    var paymentProof: PaymentProof? = null
}