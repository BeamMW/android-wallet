package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository, private val state: PaymentProofDetailsState)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    private val copyTag = "PROOF"

    override fun onViewCreated() {
        super.onViewCreated()
        state.paymentInfo = view?.getPaymentInfo()
        if (state.paymentInfo != null) {
            view?.init(state.paymentInfo!!)
        }
    }

    override fun onCopyDetails() {
        if (state.paymentInfo != null) {
            view?.copyToClipboard(view?.getDetailsContent(state.paymentInfo!!), copyTag)
            view?.showCopiedAlert()
        }
    }

    override fun onCopyProof() {
        view?.copyToClipboard(state.paymentInfo?.rawProof, copyTag)
        view?.showCopiedAlert()
    }

}