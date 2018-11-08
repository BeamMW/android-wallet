package com.mw.beam.beamwallet.transactionDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.EntitiesHelper
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import kotlinx.android.synthetic.main.item_transaction.*

/**
 * Created by vain onnellinen on 10/17/18.
 */
class TransactionDetailsFragment : BaseFragment<TransactionDetailsPresenter>(), TransactionDetailsContract.View {
    private lateinit var presenter: TransactionDetailsPresenter

    companion object {
        private const val EXTRA_TX_DESCRIPTION = "EXTRA_TX_DESCRIPTION"

        fun newInstance(txDescription: TxDescription): TransactionDetailsFragment {
            val args = Bundle()
            val fragment = TransactionDetailsFragment()
            args.putParcelable(EXTRA_TX_DESCRIPTION, txDescription)
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return TransactionDetailsFragment::class.java.simpleName
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_transaction_details, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = TransactionDetailsPresenter(this, TransactionDetailsRepository())
        configPresenter(presenter)
    }

    override fun init(txDescription: TxDescription) {
        configTransactionDetails(txDescription)
        configGeneralTransactionInfo(txDescription)
    }

    override fun getTransactionDetails(): TxDescription? {
        return arguments?.getParcelable(EXTRA_TX_DESCRIPTION)
    }

    private fun configTransactionDetails(txDescription: TxDescription) {
        val context = context ?: return

        when (txDescription.senderEnum) {
            EntitiesHelper.TxSender.RECEIVED -> {
                val receivedColor = ContextCompat.getColor(context, R.color.received_color)
                sum.setTextColor(receivedColor)
                status.setTextColor(receivedColor)
                status.text = context.getString(R.string.wallet_status_received)
                currency.setImageResource(R.drawable.beam_received)
                message.text = String.format(context.getString(R.string.wallet_transactions_receive), "BEAM") //TODO replace when multiply currency will be available
            }
            EntitiesHelper.TxSender.SENT -> {
                val sentColor = ContextCompat.getColor(context, R.color.sent_color)
                sum.setTextColor(sentColor)
                status.setTextColor(sentColor)
                status.text = context.getString(R.string.wallet_status_sent)
                currency.setImageResource(R.drawable.beam_sent)
                message.text = String.format(context.getString(R.string.wallet_transactions_send), "BEAM") //TODO replace when multiply currency will be available
            }
        }

        if (txDescription.statusEnum == EntitiesHelper.TxStatus.Failed
                || txDescription.statusEnum == EntitiesHelper.TxStatus.InProgress
                || txDescription.statusEnum == EntitiesHelper.TxStatus.Cancelled) {
            status.setTextColor(ContextCompat.getColor(context, R.color.transaction_common_status_color))
            status.text = txDescription.statusEnum.name.toLowerCase() //TODO make resources when all statuses will be stable
        }

        transactionLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.transaction_details_background))
        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime * 1000)
        sum.text = EntitiesHelper.convertToBeamWithSign(txDescription.amount, txDescription.sender)

    }

    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        if (txDescription.sender) {
            startAddress.text = EntitiesHelper.bytesToHex(txDescription.myId)
            endAddress.text = EntitiesHelper.bytesToHex(txDescription.peerId)
        } else {
            startAddress.text = EntitiesHelper.bytesToHex(txDescription.peerId)
            endAddress.text = EntitiesHelper.bytesToHex(txDescription.myId)
        }

        transactionFee.text = EntitiesHelper.convertToBeamAsFloatString(txDescription.fee)
        if (txDescription.message != null && txDescription.message.isNotEmpty()) {
            comment.text = String(txDescription.message)
            commentTitle.visibility = View.VISIBLE
            comment.visibility = View.VISIBLE
        }

    }

    private fun configTransactionHistory(txDescription: TxDescription) {
    }
}
