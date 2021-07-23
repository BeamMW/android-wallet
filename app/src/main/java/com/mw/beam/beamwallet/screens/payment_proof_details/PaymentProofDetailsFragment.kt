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

import android.annotation.SuppressLint
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.fragment_payment_proof_details.*
import com.mw.beam.beamwallet.core.AppManager
import android.view.View
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.convertToAssetStringWithId

class PaymentProofDetailsFragment : BaseFragment<PaymentProofDetailsPresenter>(), PaymentProofDetailsContract.View {

    override fun getPaymentProof(): PaymentProof = PaymentProofDetailsFragmentArgs.fromBundle(requireArguments()).paymentProof
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    @SuppressLint("SetTextI18n")
    override fun init(paymentProof: PaymentProof) {
        toolbarLayout.hasStatus = true

        senderValue.text = paymentProof.senderId
        receiverValue.text = paymentProof.receiverId
        amountValue.text = paymentProof.amount.convertToAssetStringWithId(paymentProof.assetId)
        kernelValue.text = paymentProof.kernelId
        codeValue.text = paymentProof.rawProof

        val sender = AppManager.instance.getAddress(paymentProof.senderId)
        if(sender !=null && sender.label.isNotEmpty())
        {
            senderContactLayout.visibility = View.VISIBLE
            senderContactValue.text = sender.label
        }
        else{
            senderContactLayout.visibility = View.GONE
        }

        val receiver = AppManager.instance.getAddress(paymentProof.receiverId)
        if(receiver !=null && !receiver.label.isNullOrEmpty())
        {
            receiverContactLayout.visibility = View.VISIBLE
            receiverContactValue.text = receiver.label
        }
        else{
            receiverContactLayout.visibility = View.GONE
        }
    }

    override fun getDetailsContent(paymentProof: PaymentProof): String {
        return "${getString(R.string.sender)} " +
                "${paymentProof.senderId} \n" +
                "${getString(R.string.receiver)} " +
                "${paymentProof.receiverId} \n" +
                "${getString(R.string.amount)} " +
                "${paymentProof.amount.convertToAssetStringWithId(paymentProof.assetId)} \n" +
                "${getString(R.string.kernel_id)} " +
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
        showSnackBar(getString(R.string.copied))
    }

    override fun clearListeners() {
        btnDetailsCopy.setOnClickListener(null)
        btnCodeCopy.setOnClickListener(null)
    }

    override fun getToolbarTitle(): String = getString(R.string.payment_proof)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_payment_proof_details

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
       return PaymentProofDetailsPresenter(this, PaymentProofDetailsRepository(), PaymentProofDetailsState())
    }
}