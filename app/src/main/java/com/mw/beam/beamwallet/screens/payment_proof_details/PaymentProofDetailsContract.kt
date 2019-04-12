package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import io.reactivex.subjects.Subject

interface PaymentProofDetailsContract {

    interface View: MvpView {
        fun getTransactionId(): String
        fun initDetails(txDescription: TxDescription)
        fun initProof(paymentProof: PaymentProof)
        fun copyToClipboard(content: String?)
        fun getDetailsContent(txDescription: TxDescription?): String
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyDetails()
        fun onCopyProof()
    }

    interface Repository: MvpRepository {
        fun getTxStatus(): Subject<OnTxStatusData>
        fun getPaymentProofs(txId: String): Subject<PaymentProof>
    }
}