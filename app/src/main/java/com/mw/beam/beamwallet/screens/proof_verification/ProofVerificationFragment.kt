/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.proof_verification

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.transition.AutoTransition
import android.view.View
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_proof_verification.*
import kotlinx.android.synthetic.main.fragment_proof_verification.btnDetailsCopy
import kotlinx.android.synthetic.main.fragment_proof_verification.receiverValue
import kotlinx.android.synthetic.main.fragment_proof_verification.senderValue
import kotlinx.android.synthetic.main.fragment_proof_verification.toolbarLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.kernelValue
import kotlinx.android.synthetic.main.fragment_proof_verification.amountValue
import com.mw.beam.beamwallet.core.AppModel
import kotlinx.android.synthetic.main.fragment_proof_verification.detailsArrowView
import kotlinx.android.synthetic.main.fragment_proof_verification.detailsExpandLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.detailsLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.kernelLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.amountLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.senderLayout
import kotlinx.android.synthetic.main.fragment_proof_verification.receiverLayout


class ProofVerificationFragment : BaseFragment<ProofVerificationPresenter>(), ProofVerificationContract.View {
    private lateinit var textWatcher: TextWatcher

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_proof_verification

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_verification)

    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun addListeners() {
        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                presenter?.onProofCodeChanged(s.toString())
            }
        }

        proofValue.addListener(textWatcher)
        btnDetailsCopy.setOnClickListener {
            presenter?.onCopyDetailsPressed()
        }

        detailsExpandLayout.setOnClickListener {
            presenter?.onExpandDetailsPressed()
        }
    }

    override fun init() {
        toolbarLayout.hasStatus = true
    }

    override fun clear() {
        TransitionManager.beginDelayedTransition(proofContainer)
        detailsLayout.visibility = View.GONE
        btnDetailsCopy.visibility = View.GONE
    }

    override fun showErrorProof() {
        proofError.visibility = View.VISIBLE

        val errorColorStateList = ColorStateList.valueOf(context!!.getColor(R.color.common_error_color))
        proofValue.backgroundTintList = errorColorStateList
        proofValue.setTextColor(errorColorStateList)
    }

    override fun hideErrorProof() {
        proofError.visibility = View.GONE

        proofValue.backgroundTintList = ColorStateList.valueOf(context!!.getColor(R.color.white_01))
        proofValue.setTextColor(context!!.getColor(R.color.common_text_color))
    }

    @SuppressLint("SetTextI18n")
    override fun showProof(proof: PaymentProof) {
        proofValue.clearFocus()
        hideKeyboard()
        btnDetailsCopy.requestFocus()

        senderValue.text = proof.senderId
        receiverValue.text = proof.receiverId

        val sender = AppModel.instance.getAddress(proof.senderId)
        if(sender !=null && !sender.label.isNullOrEmpty())
        {
            senderContactLayout.visibility = View.VISIBLE
            senderContactValue.text = sender.label
        }
        else{
            senderContactLayout.visibility = View.GONE
        }

        val receiver = AppModel.instance.getAddress(proof.receiverId)
        if(receiver !=null && !receiver.label.isNullOrEmpty())
        {
            receiverContactLayout.visibility = View.VISIBLE
            receiverContactValue.text = receiver.label
        }
        else{
            receiverContactLayout.visibility = View.GONE
        }


        amountValue.text = "${proof.amount.convertToBeamString()} ${getString(R.string.currency_beam)}".toUpperCase()
        kernelValue.text = proof.kernelId

        TransitionManager.beginDelayedTransition(proofContainer)
        detailsLayout.visibility = View.VISIBLE
        btnDetailsCopy.visibility = View.VISIBLE

        if (senderLayout.visibility == View.GONE)
        {
            presenter?.onExpandDetailsPressed()
        }
    }

    override fun showCopiedMessage() {
        showSnackBar(getString(R.string.copied))
    }

    override fun getDetailsContent(proof: PaymentProof): String {
        return "${getString(R.string.sender)} " +
                "${proof.senderId} \n" +
                "${getString(R.string.receiver)} " +
                "${proof.receiverId} \n" +
                "${getString(R.string.amount)} " +
                "${(proof.amount.convertToBeamString() + getString(R.string.currency_beam)).toUpperCase()} \n" +
                "${getString(R.string.kernel_id)} " +
                proof.kernelId
    }

    override fun handleExpandDetails(shouldExpandDetails: Boolean) {
        animateDropDownIcon(detailsArrowView, !shouldExpandDetails)

        android.transition.TransitionManager.beginDelayedTransition(proofContainer, AutoTransition().apply {
        })

        val contentVisibility = if (shouldExpandDetails) View.VISIBLE else View.GONE
        receiverLayout.visibility = contentVisibility
        senderLayout.visibility = contentVisibility
        kernelLayout.visibility = contentVisibility
        amountLayout.visibility = contentVisibility
    }

    override fun clearListeners() {
        btnDetailsCopy.setOnClickListener(null)
        detailsExpandLayout.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ProofVerificationPresenter(this, ProofVerificationRepository(), ProofVerificationState())
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }
}