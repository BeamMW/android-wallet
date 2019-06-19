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

import android.content.res.ColorStateList
import android.text.Editable
import android.view.View
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

class ProofVerificationFragment : BaseFragment<ProofVerificationPresenter>(), ProofVerificationContract.View {
    private lateinit var textWatcher: TextWatcher

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_proof_verification

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_verification)

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
    }

    override fun clear() {
        TransitionManager.beginDelayedTransition(proofContainer)
        proofGroup.visibility = View.GONE
    }

    override fun showErrorProof() {
        proofError.visibility = View.VISIBLE

        val errorColorStateList = ColorStateList.valueOf(context!!.getColor(R.color.common_error_color))
        proofValue.backgroundTintList = errorColorStateList
        proofValue.setTextColor(errorColorStateList)
    }

    override fun hideErrorProof() {
        proofError.visibility = View.GONE
        proofValue.backgroundTintList = ColorStateList.valueOf(context!!.getColor(R.color.colorAccent))
        proofValue.setTextColor(context!!.getColor(R.color.common_text_color))
    }

    override fun showProof(proof: PaymentProof) {
        senderValue.text = proof.senderId
        receiverValue.text = proof.receiverId
        amountValue.text = (proof.amount.convertToBeamString() + getString(R.string.currency_beam)).toLowerCase()
        kernelIdValue.text = proof.kernelId

        TransitionManager.beginDelayedTransition(proofContainer)
        proofGroup.visibility = View.VISIBLE
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

    override fun clearListeners() {
        btnDetailsCopy.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ProofVerificationPresenter(this, ProofVerificationRepository(), ProofVerificationState())
    }
}