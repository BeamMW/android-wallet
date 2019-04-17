package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository, private val state: PaymentProofDetailsState)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    private val COPY_TAG = "PROOF"

    override fun onViewCreated() {
        super.onViewCreated()
        state.paymentProof = view?.getPaymentProof()
        if (state.paymentProof != null) {
            view?.init(state.paymentProof!!)
        }
    }

    override fun onCopyDetails() {
        if (state.paymentProof != null) {
            view?.copyToClipboard(view?.getDetailsContent(state.paymentProof!!), COPY_TAG)
            view?.showCopiedAlert()
        }
    }

    override fun onCopyProof() {
        view?.copyToClipboard(state.paymentProof?.rawProof, COPY_TAG)
        view?.showCopiedAlert()
    }

}