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

package com.mw.beam.beamwallet.transaction_details

import android.support.v4.content.ContextCompat
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.activity_transaction_details.*
import kotlinx.android.synthetic.main.item_transaction.*

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

    private fun configTransactionDetails(txDescription: TxDescription) {
        when (txDescription.sender) {
            TxSender.RECEIVED -> {
                sum.setTextColor(ContextCompat.getColor(this, R.color.received_color))
                status.setTextColor(ContextCompat.getColor(this, R.color.received_color))
                currency.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.currency_beam_receive))
                message.text = String.format(getString(R.string.wallet_transactions_receive), "BEAM") //TODO replace when multiply currency will be available
            }
            TxSender.SENT -> {
                sum.setTextColor(ContextCompat.getColor(this, R.color.sent_color))
                status.setTextColor(ContextCompat.getColor(this, R.color.sent_color))
                currency.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.currency_beam_send))
                message.text = String.format(getString(R.string.wallet_transactions_send), "BEAM") //TODO replace when multiply currency will be available
            }
        }

        status.text = when (txDescription.status) {
            TxStatus.InProgress -> getString(R.string.wallet_status_in_progress)
            TxStatus.Cancelled -> getString(R.string.wallet_status_cancelled)
            TxStatus.Failed -> getString(R.string.wallet_status_failed)
            TxStatus.Pending -> getString(R.string.wallet_status_pending)
            TxStatus.Registered -> getString(R.string.wallet_status_syncing_with_blockchain)
            TxStatus.Completed -> getString(R.string.wallet_status_completed)
        }

        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime * 1000)
        sum.text = txDescription.amount.convertToBeamWithSign(txDescription.sender.value)
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
        kernel.text = txDescription.kernelId

        if (txDescription.message.isNotEmpty()) {
            comment.text = txDescription.message
            commentTitle.visibility = View.VISIBLE
            comment.visibility = View.VISIBLE
        }
    }

    private fun configTransactionHistory(txDescription: TxDescription) {
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = TransactionDetailsPresenter(this, TransactionDetailsRepository())
        return presenter
    }
}
