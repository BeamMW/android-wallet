package com.mw.beam.beamwallet.transactionDetails

import android.support.v4.content.ContextCompat
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamAsFloatString
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
        when (txDescription.senderEnum) {
            TxSender.RECEIVED -> {
                sum.setTextColor(ContextCompat.getColor(this, R.color.received_color))
                status.setTextColor(ContextCompat.getColor(this, R.color.received_color))
                message.text = String.format(getString(R.string.wallet_transactions_receive), "BEAM") //TODO replace when multiply currency will be available
            }
            TxSender.SENT -> {
                sum.setTextColor(ContextCompat.getColor(this, R.color.sent_color))
                status.setTextColor(ContextCompat.getColor(this, R.color.sent_color))
                message.text = String.format(getString(R.string.wallet_transactions_send), "BEAM") //TODO replace when multiply currency will be available
            }
        }

        status.text = when (txDescription.statusEnum) {
            TxStatus.InProgress -> getString(R.string.wallet_status_in_progress)
            TxStatus.Cancelled -> getString(R.string.wallet_status_cancelled)
            TxStatus.Failed -> getString(R.string.wallet_status_failed)
            TxStatus.Pending -> getString(R.string.wallet_status_pending)
            TxStatus.Registered -> getString(R.string.wallet_status_confirming)
            TxStatus.Completed -> when (txDescription.senderEnum) {
                TxSender.RECEIVED -> getString(R.string.wallet_status_received)
                TxSender.SENT -> getString(R.string.wallet_status_sent)
            }
        }

        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime * 1000)
        sum.text = txDescription.amount.convertToBeamWithSign(txDescription.sender)
    }

    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        if (txDescription.sender) {
            startAddress.text = txDescription.myId
            endAddress.text = txDescription.peerId
        } else {
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        transactionFee.text = txDescription.fee.convertToBeamAsFloatString()
        if (txDescription.message != null && txDescription.message.isNotEmpty()) {
            comment.text = String(txDescription.message)
            commentTitle.visibility = View.VISIBLE
            comment.visibility = View.VISIBLE
        }
    }

    private fun configTransactionHistory(txDescription: TxDescription) {
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = TransactionDetailsPresenter(this, TransactionDetailsRepository())
        return presenter
    }
}
