package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.BasePresenter
import io.reactivex.disposables.Disposable

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository, private val state: PaymetProofDetailsState)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    private lateinit var txUpdateSubscription: Disposable
    private lateinit var paymentProofSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        state.txId = view?.getTransactionId()
    }

    override fun initSubscriptions() {
        txUpdateSubscription = repository.getTxStatus().subscribe { data ->
            data.tx?.firstOrNull { it.id == state.txId }?.let {
                state.txDescription = it
                view?.initDetails(it)
            }
        }

        paymentProofSubscription = repository.getPaymentProofs(view!!.getTransactionId()).subscribe {
            if (it.txId == state.txId) {
                state.paymentProof = it
                view?.initProof(it)
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txUpdateSubscription, paymentProofSubscription)

    override fun onCopyDetails() {
        view?.copyToClipboard(view?.getDetailsContent(state.txDescription))
    }

    override fun onCopyProof() {
        view?.copyToClipboard(state.paymentProof?.proof)
    }

}