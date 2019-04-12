package com.mw.beam.beamwallet.screens.transaction_details

import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription

class TransactionDetailsState {
    var txDescription: TxDescription? = null
    var paymentProof: PaymentProof? = null
}