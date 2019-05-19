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

package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.activity_payment_proof_details.*

class PaymentProofDetailsActivity : BaseActivity<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {
    companion object {
        const val KEY_PAYMENT_PROOF = "KEY_PAYMENT_PROOF"
    }

    override fun getPaymentProof(): PaymentProof = intent.getParcelableExtra(KEY_PAYMENT_PROOF)

    override fun init(paymentProof: PaymentProof) {
        senderValue.text = paymentProof.senderId
        receiverValue.text = paymentProof.receiverId
        amountValue.text = getString(R.string.payment_proof_details_beam, paymentProof.amount.convertToBeamString())
        kernelIdValue.text = paymentProof.kernelId
        codeValue.text = paymentProof.rawProof
    }

    override fun getDetailsContent(paymentProof: PaymentProof): String {
        return "${getString(R.string.payment_proof_details_sender)} " +
                "${paymentProof.senderId} \n" +
                "${getString(R.string.payment_proof_details_receiver)} " +
                "${paymentProof.receiverId} \n" +
                "${getString(R.string.payment_proof_details_amount)} " +
                "${getString(R.string.payment_proof_details_beam, paymentProof.amount.convertToBeamString()).toUpperCase()} \n" +
                "${getString(R.string.payment_proof_details_kernel_id)} " +
                paymentProof.kernelId
    }

    override fun addListeners() {
        btnCodeCopy.setOnClickListener {
            presenter?.onCopyProof()
        }

        btnDetailsCopy.setOnClickListener {
            presenter?.onCopyDetails()
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
       return PaymentProofDetailsPresenter(this, PaymentProofDetailsRepository(), PaymentProofDetailsState())
    }
}