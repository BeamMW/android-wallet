package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    companion object {
        private const val COPY_TAG = "PROOF"
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            init(getProof(), getSender(), getReceiver(), getAmount(), getKernelId())
        }
    }

    override fun onCopyDetails() {
        view?.apply {
            copyToClipboard(getDetailsContent(getSender(), getReceiver(), getAmount(), getKernelId()), COPY_TAG)
        }
    }

    override fun onCopyProof() {
        view?.copyToClipboard(view?.getProof(), COPY_TAG)
    }

}