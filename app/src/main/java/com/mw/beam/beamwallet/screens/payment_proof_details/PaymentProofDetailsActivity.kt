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

class PaymentProofDetailsActivity : BaseActivity<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {
    private lateinit var presenter: PaymentProofDetailsPresenter

    companion object {
        const val KEY_TRANSACTION_ID = "KEY_TRANSACTION_ID"
        const val KEY_AMOUNT = "KEY_AMOUNT"
        const val KEY_SENDER = "KEY_SENDER"
        const val KEY_RECEIVER = "KEY_RECEIVER"
        const val KEY_KERNEL_ID = "KEY_KERNEL_ID"
        const val KEY_PROOF = "KEY_PROOF"
    }

    override fun getTransactionId(): String = intent.getStringExtra(KEY_TRANSACTION_ID)
    override fun getAmount(): Long = intent.getLongExtra(KEY_AMOUNT, 0L)
    override fun getKernelId(): String = intent.getStringExtra(KEY_KERNEL_ID)
    override fun getSender(): String = intent.getStringExtra(KEY_SENDER)
    override fun getReceiver(): String = intent.getStringExtra(KEY_RECEIVER)
    override fun getProof(): String = intent.getStringExtra(KEY_PROOF)

    override fun init(proof: String, sender: String, receiver: String, amount: Long, kernelId: String) {
        proofDetailsSenderValue.text = sender
        proofDetailsReceiverValue.text = receiver
        proofDetailsAmountValue.text = getString(R.string.payment_proof_details_beam, amount.convertToBeamString())
        proofDetailsKernelIdValue.text = kernelId
        proofDetailsCodeValue.text = proof
    }

    override fun getDetailsContent(sender: String, receiver: String, amount: Long, kernelId: String): String {
        return "${getString(R.string.payment_proof_details_sender)} \n" +
                "$sender \n" +
                "${getString(R.string.payment_proof_details_receiver)} \n" +
                "$receiver \n" +
                getString(R.string.payment_proof_details_amount) +
                "${getString(R.string.payment_proof_details_beam, amount.convertToBeamString())} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} \n" +
                kernelId
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

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_details_toolbar_title)

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_payment_proof_details

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = PaymentProofDetailsPresenter(this, PaymentProofDetailsRepository())
        return presenter
    }
}