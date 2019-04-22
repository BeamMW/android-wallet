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

import android.support.transition.TransitionManager
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.activity_proof_verification.*

class ProofVerificationActivity : BaseActivity<ProofVerificationPresenter>(), ProofVerificationContract.View {
    private lateinit var presenter: ProofVerificationPresenter

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_proof_verification

    override fun getToolbarTitle(): String? = getString(R.string.payment_proof_verification_toolbar_title)

    override fun addListeners() {
        btnDetailsCopy.setOnClickListener {
            presenter.onCopyDetailsPressed()
        }
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

    override fun showProof(proof: PaymentProof) {
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

    override fun getDetailsContent(proof: PaymentProof): String {
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
        btnDetailsCopy.setOnClickListener(null)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = ProofVerificationPresenter(this, ProofVerificationRepository(), ProofVerificationState())
        return presenter
    }
}