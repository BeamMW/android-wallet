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
        fun getSender(): String
        fun getReceiver(): String
        fun getKernelId(): String
        fun getProof(): String
        fun getAmount(): Long
        fun init(proof: String, sender: String, receiver: String, amount: Long, kernelId: String)
        fun getDetailsContent(sender: String, receiver: String, amount: Long, kernelId: String): String
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyDetails()
        fun onCopyProof()
    }

    interface Repository: MvpRepository
}