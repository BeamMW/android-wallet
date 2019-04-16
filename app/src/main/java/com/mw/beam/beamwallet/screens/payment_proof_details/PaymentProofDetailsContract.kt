package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription

interface PaymentProofDetailsContract {

    interface View: MvpView {
        fun getProof(): String
        fun getTransactionDetails(): TxDescription
        fun init(proof: String, txDescription: TxDescription)
        fun getDetailsContent(txDescription: TxDescription): String
        fun showCopiedAlert()
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyDetails()
        fun onCopyProof()
    }

    interface Repository: MvpRepository
}