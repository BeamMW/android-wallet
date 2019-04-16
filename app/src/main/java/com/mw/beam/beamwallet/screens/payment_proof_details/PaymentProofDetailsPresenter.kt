package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository, private val state: PaymentProofDetailsState)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    private val copyTag = "PROOF"

    override fun onViewCreated() {
        super.onViewCreated()
        state.txDescription = view?.getTransactionDetails()
        state.paymentProof = view?.getProof()
        if (state.paymentProof != null && state.txDescription != null) {
            view?.init(state.paymentProof!!, state.txDescription!!)
        }
    }

    override fun onCopyDetails() {
        if (state.txDescription != null) {
            view?.copyToClipboard(view?.getDetailsContent(state.txDescription!!), copyTag)
            view?.showCopiedAlert()
        }
    }

    override fun onCopyProof() {
        view?.copyToClipboard(view?.getProof(), copyTag)
        view?.showCopiedAlert()
    }

}