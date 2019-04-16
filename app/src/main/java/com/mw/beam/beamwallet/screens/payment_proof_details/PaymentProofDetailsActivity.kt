package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.activity_payment_proof_details.*

class PaymentProofDetailsActivity : BaseActivity<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {
    private lateinit var presenter: PaymentProofDetailsPresenter

    companion object {
        const val KEY_TX_DESCRIPTION = "KEY_TX_DESCRIPTION"
        const val KEY_PROOF = "KEY_PROOF"
    }

    override fun getTransactionDetails(): TxDescription = intent.getParcelableExtra(KEY_TX_DESCRIPTION)
    override fun getProof(): String = intent.getStringExtra(KEY_PROOF)

    override fun init(proof: String, txDescription: TxDescription) {
        senderValue.text = if (txDescription.sender.value) txDescription.myId else txDescription.peerId
        receiverValue.text = if (txDescription.sender.value) txDescription.peerId else txDescription.myId
        amountValue.text = getString(R.string.payment_proof_details_beam, txDescription.amount.convertToBeamString())
        kernelIdValue.text = txDescription.kernelId
        codeValue.text = proof
    }

    override fun getDetailsContent(txDescription: TxDescription): String {
        return "${getString(R.string.payment_proof_details_sender)} " +
                "${if (txDescription.sender.value) txDescription.myId else txDescription.peerId} \n" +
                "${getString(R.string.payment_proof_details_receiver)} " +
                "${if (txDescription.sender.value) txDescription.peerId else txDescription.myId} \n" +
                "${getString(R.string.payment_proof_details_amount)} " +
                "${getString(R.string.payment_proof_details_beam, txDescription.amount.convertToBeamString()).toUpperCase()} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} " +
                txDescription.kernelId
    }

    override fun addListeners() {
        btnCodeCopy.setOnClickListener {
            presenter.onCopyProof()
        }

        btnDetailsCopy.setOnClickListener {
            presenter.onCopyDetails()
        }
    }

    override fun showCopiedAlert() {
        showSnackBar(getString(R.string.common_copied_alert))
    }

    override fun clearListeners() {
        btnDetailsCopy.setOnClickListener(null)
        btnCodeCopy.setOnClickListener(null)
    }

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_details_toolbar_title)

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_payment_proof_details

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = PaymentProofDetailsPresenter(this, PaymentProofDetailsRepository(), PaymentProofDetailsState())
        return presenter
    }
}