package com.mw.beam.beamwallet.screens.proof_verification

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO

class ProofVerificationRepository: BaseRepository(), ProofVerificationContract.Repository {

    override fun getVerifyPaymentProof(proof: String): PaymentInfoDTO? {
        return wallet?.verifyPaymentInfo(proof)
    }

}