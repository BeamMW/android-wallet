package com.mw.beam.beamwallet.screens.proof_verification

import android.content.ClipboardManager
import android.content.Context
import android.support.transition.TransitionManager
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.views.PasteEditTextWatcher
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.activity_proof_verification.*

class ProofVerificationActivity : BaseActivity<ProofVerificationPresenter>(), ProofVerificationContract.View {
    private lateinit var presenter: ProofVerificationPresenter
    private lateinit var textWatcher: TextWatcher

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_proof_verification

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_verification_toolbar_title)

    override fun addListeners() {
        textWatcher = object : PasteEditTextWatcher {
            override fun onPaste() {
                val clipboardManager = baseContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                if (clipboardManager.hasPrimaryClip()) {
                    val item = clipboardManager.primaryClip?.getItemAt(0)
                    presenter.onProofCodeChanged(item?.text.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
                presenter.onProofCodeChanged(s.toString())
            }
        }

        proofValue.addListener(textWatcher)
    }

    override fun clear() {
        TransitionManager.beginDelayedTransition(proofContainer)
        proofGroup.visibility = View.GONE
    }

    override fun showErrorProof() {
        proofError.visibility = View.VISIBLE
    }

    override fun hideErrorProof() {
        proofError.visibility = View.GONE
    }

    override fun showProof(proof: PaymentInfoDTO) {
        senderValue.text = proof.senderId
        receiverValue.text = proof.receiverId
        amountValue.text = getString(R.string.payment_proof_details_beam, proof.amount.convertToBeamString())
        kernelIdValue.text = proof.kernelId

        TransitionManager.beginDelayedTransition(proofContainer)
        proofGroup.visibility = View.VISIBLE
    }

    override fun showCopiedMessage() {
        showSnackBar(getString(R.string.common_copied_alert))
    }

    override fun getDetailsContent(proof: PaymentInfoDTO): String {
        return "${getString(R.string.payment_proof_details_sender)} " +
                "${proof.senderId} \n" +
                "${getString(R.string.payment_proof_details_receiver)} " +
                "${proof.receiverId} \n" +
                "${getString(R.string.payment_proof_details_amount)} " +
                "${getString(R.string.payment_proof_details_beam, proof.amount.convertToBeamString()).toUpperCase()} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} " +
                proof.kernelId
    }

    override fun clearListeners() {
        proofValue.removeListener(textWatcher)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = ProofVerificationPresenter(this, ProofVerificationRepository(), ProofVerificationState())
        return presenter
    }
}