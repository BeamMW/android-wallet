package com.mw.beam.beamwallet.screens.proof_verification

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO

interface ProofVerificationContract {

    interface View: MvpView {
        fun showErrorProof()
        fun hideErrorProof()
        fun showProof(proof: PaymentInfoDTO)
        fun showCopiedMessage()
        fun getDetailsContent(proof: PaymentInfoDTO): String
        fun clear()
    }

    interface Presenter: MvpPresenter<View> {
        fun onProofCodeChanged(proof: String)
        fun onCopyDetailsPressed()
    }

    interface Repository: MvpRepository {
        fun getVerifyPaymentProof(proof: String): PaymentInfoDTO?
    }

}