package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentInfo
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.activity_payment_proof_details.*

class PaymentProofDetailsActivity : BaseActivity<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {
    private lateinit var presenter: PaymentProofDetailsPresenter

    companion object {
        const val KEY_PAYMENT_INFO = "KEY_PAYMENT_INFO"
    }

    override fun getPaymentInfo(): PaymentInfo = intent.getParcelableExtra(KEY_PAYMENT_INFO)

    override fun init(paymentInfo: PaymentInfo) {
        senderValue.text = paymentInfo.senderId
        receiverValue.text = paymentInfo.receiverId
        amountValue.text = getString(R.string.payment_proof_details_beam, paymentInfo.amount.convertToBeamString())
        kernelIdValue.text = paymentInfo.kernelId
        codeValue.text = paymentInfo.rawProof
    }

    override fun getDetailsContent(paymentInfo: PaymentInfo): String {
        return "${getString(R.string.payment_proof_details_sender)} " +
                "${paymentInfo.senderId} \n" +
                "${getString(R.string.payment_proof_details_receiver)} " +
                "${paymentInfo.receiverId} \n" +
                "${getString(R.string.payment_proof_details_amount)} " +
                "${getString(R.string.payment_proof_details_beam, paymentInfo.amount.convertToBeamString()).toUpperCase()} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} " +
                paymentInfo.kernelId
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