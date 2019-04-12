package com.mw.beam.beamwallet.screens.payment_proof_details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.activity_payment_proof_details.*

class PaymentProofDetailsActivity: BaseActivity<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {
    private lateinit var presenter: PaymentProofDetailsPresenter

    companion object {
        const val TRANSACTION_ID = "TRANSACTION_ID"
        private const val COPY_TAG = "PROOF"
    }

    override fun getTransactionId(): String = intent.getStringExtra(TRANSACTION_ID)

    override fun copyToClipboard(content: String?) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(COPY_TAG, content)
    }

    override fun initDetails(txDescription: TxDescription) {
        if (txDescription.sender.value) {
            proofDetailsSenderValue.text = txDescription.myId
            proofDetailsReceiverValue.text = txDescription.peerId
        } else {
            proofDetailsSenderValue.text = txDescription.peerId
            proofDetailsReceiverValue.text = txDescription.myId
        }

        proofDetailsAmountValue.text = getString(R.string.payment_proof_details_beam, txDescription.amount.convertToBeamString())
        proofDetailsKernelIdValue.text = txDescription.kernelId
    }

    override fun getDetailsContent(txDescription: TxDescription?): String {
        if (txDescription == null) return ""
        val sender: String
        val receiver: String
        if (txDescription.sender.value) {
            sender = txDescription.myId
            receiver = txDescription.peerId
        } else {
            sender = txDescription.peerId
            receiver = txDescription.myId
        }
        return "${getString(R.string.payment_proof_details_sender)} \n" +
                "$sender \n" +
                "${getString(R.string.payment_proof_details_receiver)} \n" +
                "$receiver \n" +
                getString(R.string.payment_proof_details_amount) +
                "${getString(R.string.payment_proof_details_beam, txDescription.amount.convertToBeamString())} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} \n" +
                txDescription.kernelId
    }

    override fun addListeners() {
        btnProofCodeCopy.setOnClickListener {
            presenter.onCopyProof()
        }
        btnProofDetailsCopy.setOnClickListener {
            presenter.onCopyDetails()
        }
    }

    override fun clearListeners() {
        btnProofDetailsCopy.setOnClickListener(null)
        btnProofCodeCopy.setOnClickListener(null)
    }

    override fun initProof(paymentProof: PaymentProof) {
        proofDetailsCodeValue.text = paymentProof.proof
    }

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_details_toolbar_title)

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_payment_proof_details

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = PaymentProofDetailsPresenter(this, PaymentProofDetailsRepository(), PaymetProofDetailsState())
        return presenter
    }
}