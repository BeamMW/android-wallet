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

package com.mw.beam.beamwallet.screens.transaction_details

import android.annotation.SuppressLint
import android.content.Intent
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.screens.payment_proof_details.PaymentProofDetailsActivity
import kotlinx.android.synthetic.main.activity_transaction_details.*
import kotlinx.android.synthetic.main.item_transaction.*
import kotlinx.android.synthetic.main.item_transaction_utxo.view.*

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsActivity : BaseActivity<TransactionDetailsPresenter>(), TransactionDetailsContract.View {
    private lateinit var presenter: TransactionDetailsPresenter

    companion object {
        const val EXTRA_TRANSACTION_DETAILS = "EXTRA_TRANSACTION_DETAILS"
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_transaction_details
    override fun getToolbarTitle(): String? = getString(R.string.transaction_details_title)
    override fun getTransactionDetails(): TxDescription = intent.getParcelableExtra(EXTRA_TRANSACTION_DETAILS)

    override fun init(txDescription: TxDescription) {
        configTransactionDetails(txDescription)
        configGeneralTransactionInfo(txDescription)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        presenter.onMenuCreate(menu)

        return true
    }

    override fun configMenuItems(menu: Menu?, txStatus: TxStatus) {
        if (TxStatus.InProgress == txStatus
                || TxStatus.Pending == txStatus
                || TxStatus.Failed == txStatus
                || TxStatus.Completed == txStatus
                || TxStatus.Cancelled == txStatus) {
            menuInflater.inflate(R.menu.transaction_menu, menu)
            menu?.findItem(R.id.cancel)?.isVisible = TxStatus.InProgress == txStatus || TxStatus.Pending == txStatus
            menu?.findItem(R.id.delete)?.isVisible = TxStatus.Failed == txStatus || TxStatus.Completed == txStatus || TxStatus.Cancelled == txStatus
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // R.id.repeat -> {  }
            // R.id.save -> {  }
            R.id.cancel -> presenter.onCancelTransaction()
            R.id.delete -> presenter.onDeleteTransaction()
        }

        return true
    }

    @SuppressLint("InflateParams")
    override fun updateUtxos(utxoInfoList: List<UtxoInfoItem>) {
        transactionUtxoContainer.visibility = if (utxoInfoList.isEmpty()) View.GONE else View.VISIBLE
        transactionUtxoList.removeAllViews()

        utxoInfoList.forEach { utxo ->
            val utxoView = LayoutInflater.from(this).inflate(R.layout.item_transaction_utxo, null)

            val drawableId = when(utxo.type) {
                UtxoType.Send -> R.drawable.ic_history_sent
                UtxoType.Receive -> R.drawable.ic_history_received
                UtxoType.Exchange -> R.drawable.menu_utxo
            }

            utxoView.utxoIcon.setImageDrawable(getDrawable(drawableId))
            utxoView.utxoAmount.text = utxo.amount.convertToBeamString()

            transactionUtxoList.addView(utxoView)
        }
    }

    private fun configTransactionDetails(txDescription: TxDescription) {
        message.text = String.format(
                when (txDescription.sender) {
                    TxSender.RECEIVED -> getString(R.string.wallet_transactions_receive)
                    TxSender.SENT -> getString(R.string.wallet_transactions_send)
                },
                getString(R.string.currency_beam).toUpperCase()) //TODO replace when multiply currency will be available

        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime)
        currency.setImageDrawable(txDescription.currencyImage)

        sum.text = txDescription.amount.convertToBeamWithSign(txDescription.sender.value)
        sum.setTextColor(txDescription.amountColor)

        status.setTextColor(txDescription.statusColor)
        status.text = txDescription.statusString
    }

    override fun updatePaymentProof(paymentProof: PaymentProof) {
        if (paymentProofContainer.visibility == View.VISIBLE) return

        TransitionManager.beginDelayedTransition(transactionDetailsMainContainer)
        paymentProofContainer.visibility = View.VISIBLE
    }

    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        if (txDescription.sender.value) {
            startAddress.text = txDescription.myId
            endAddress.text = txDescription.peerId
        } else {
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        transactionFee.text = txDescription.fee.convertToBeamString()
        transactionId.text = txDescription.id
        kernel.text = txDescription.kernelId

        if (txDescription.message.isNotEmpty()) {
            comment.text = txDescription.message
            commentTitle.visibility = View.VISIBLE
            comment.visibility = View.VISIBLE
        }
    }

    override fun addListeners() {
        btnPaymentProofDetails.setOnClickListener {
            presenter.onShowPaymentProof()
        }

        btnPaymentProofCopy.setOnClickListener {
            presenter.onCopyPaymentProof()
        }
    }

    override fun showPaymentProof(paymentProof: PaymentProof) {
        val intent = Intent(this, PaymentProofDetailsActivity::class.java).apply {
            putExtra(PaymentProofDetailsActivity.KEY_PAYMENT_PROOF, paymentProof)
        }

        startActivity(intent)
    }

    override fun showCopiedAlert() {
        showSnackBar(getString(R.string.common_copied_alert))
    }

    override fun clearListeners() {
        btnPaymentProofDetails.setOnClickListener(null)
        btnPaymentProofCopy.setOnClickListener(null)
    }

    override fun finishScreen() {
        finish()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = TransactionDetailsPresenter(this, TransactionDetailsRepository(), TransactionDetailsState())
        return presenter
    }
}
