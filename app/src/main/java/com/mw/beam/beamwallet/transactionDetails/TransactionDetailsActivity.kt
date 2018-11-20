package com.mw.beam.beamwallet.transactionDetails

import android.support.v4.content.ContextCompat
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.*
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
    override fun getTransactionDetails(): TxDescription = intent.getParcelableExtra(EXTRA_TRANSACTION_DETAILS)

    override fun init(txDescription: TxDescription) {
        initToolbar(toolbar, getString(R.string.transaction_details_title))
        configTransactionDetails(txDescription)
        configGeneralTransactionInfo(txDescription)
    }

    private fun configTransactionDetails(txDescription: TxDescription) {
        when (txDescription.senderEnum) {
            TxSender.RECEIVED -> {
                val receivedColor = ContextCompat.getColor(this, R.color.received_color)
                sum.setTextColor(receivedColor)
                status.setTextColor(receivedColor)
                status.text = getString(R.string.wallet_status_received)
                currency.setImageResource(R.drawable.beam_received)
                message.text = String.format(getString(R.string.wallet_transactions_receive), "BEAM") //TODO replace when multiply currency will be available
            }
            TxSender.SENT -> {
                val sentColor = ContextCompat.getColor(this, R.color.sent_color)
                sum.setTextColor(sentColor)
                status.setTextColor(sentColor)
                status.text = getString(R.string.wallet_status_sent)
                currency.setImageResource(R.drawable.beam_sent)
                message.text = String.format(getString(R.string.wallet_transactions_send), "BEAM") //TODO replace when multiply currency will be available
            }
        }

        if (txDescription.statusEnum == TxStatus.Failed
                || txDescription.statusEnum == TxStatus.InProgress
                || txDescription.statusEnum == TxStatus.Cancelled) {
            status.setTextColor(ContextCompat.getColor(this, R.color.transaction_common_status_color))
            status.text = txDescription.statusEnum.name.toLowerCase() //TODO make resources when all statuses will be stable
        }

        transactionLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transaction_details_background))
        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime * 1000)
        sum.text = txDescription.amount.convertToBeamWithSign(txDescription.sender)
    }

    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        if (txDescription.sender) {
            startAddress.text = txDescription.myId.toHex()
            endAddress.text = txDescription.peerId.toHex()
        } else {
            startAddress.text = txDescription.peerId.toHex()
            endAddress.text = txDescription.myId.toHex()
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
