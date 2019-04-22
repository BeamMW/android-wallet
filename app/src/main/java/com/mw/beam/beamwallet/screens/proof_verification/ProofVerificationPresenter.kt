package com.mw.beam.beamwallet.screens.proof_verification

import com.mw.beam.beamwallet.base_screen.BasePresenter

class ProofVerificationPresenter(view: ProofVerificationContract.View?, repository: ProofVerificationContract.Repository, private val state: ProofVerificationState)
    : BasePresenter<ProofVerificationContract.View, ProofVerificationContract.Repository>(view, repository), ProofVerificationContract.Presenter {
    private val COPY_TAG = "PROOF_VERIFICATION"

    override fun onProofCodeChanged(proof: String) {
        if (proof.isEmpty()) {
            view?.clear()
            view?.hideErrorProof()
            return
        }

        state.proof = repository.getVerifyPaymentProof(proof)

        if (state.proof?.isValid == true) {
            view?.hideErrorProof()
            state.proof?.let { view?.showProof(it) }
        } else {
            view?.showErrorProof()
            view?.clear()
        }
    }

    override fun onCopyDetailsPressed() {
        view?.copyToClipboard(view?.getDetailsContent(state.proof ?: return), COPY_TAG)
        view?.showCopiedMessage()
    }
}