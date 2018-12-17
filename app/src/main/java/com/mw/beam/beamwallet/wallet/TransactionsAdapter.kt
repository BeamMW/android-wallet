package com.mw.beam.beamwallet.wallet

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*

/**
 * Created by vain onnellinen on 10/2/18.
 */
class TransactionsAdapter(private val context: Context, private var data: List<TxDescription>, private val clickListener: OnItemClickListener) :
        RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private val beamResId = R.drawable.ic_beam
    private val receivedColor = ContextCompat.getColor(context, R.color.received_color)
    private val receivedStatus = context.getString(R.string.wallet_status_received)
    private val sentStatus = context.getString(R.string.wallet_status_sent)
    private val inProgressStatus = context.getString(R.string.wallet_status_in_progress)
    private val cancelledStatus = context.getString(R.string.wallet_status_cancelled)
    private val failedStatus = context.getString(R.string.wallet_status_failed)
    private val pendingStatus = context.getString(R.string.wallet_status_pending)
    private val confirmingStatus = context.getString(R.string.wallet_status_confirming)
    private val sentColor = ContextCompat.getColor(context, R.color.sent_color)
    private val swapStatusColor = ContextCompat.getColor(context, R.color.transaction_common_status_color)
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_not_multiply_color)
    private val receiveText = context.getString(R.string.wallet_transactions_receive)
    private val sendText = context.getString(R.string.wallet_transactions_send)
    private val swapText = context.getString(R.string.wallet_transactions_swap)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = data[position]

        holder.apply {
            when (transaction.senderEnum) {
                TxSender.RECEIVED -> {
                    sum.setTextColor(receivedColor)
                    status.setTextColor(receivedColor)
                    message.text = String.format(receiveText, "BEAM") //TODO replace when multiply currency will be available
                }
                TxSender.SENT -> {
                    sum.setTextColor(sentColor)
                    status.setTextColor(sentColor)
                    message.text = String.format(sendText, "BEAM") //TODO replace when multiply currency will be available
                }
            }

            status.text = when (transaction.statusEnum) {
                TxStatus.InProgress -> inProgressStatus
                TxStatus.Cancelled -> cancelledStatus
                TxStatus.Failed -> failedStatus
                TxStatus.Pending -> pendingStatus
                TxStatus.Registered -> confirmingStatus
                TxStatus.Completed -> when (transaction.senderEnum) {
                    TxSender.RECEIVED -> receivedStatus
                    TxSender.SENT -> sentStatus
                }
            }

            itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor)
            icon.setImageResource(beamResId)
            date.text = CalendarUtils.fromTimestamp(transaction.modifyTime * 1000)
            sum.text = transaction.amount.convertToBeamWithSign(transaction.sender)
        }
    }

    override fun getItemCount(): Int = data.size
    fun getPositionByItem(item: TxDescription) = data.indexOf(item)

    fun setData(data: List<TxDescription>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: TxDescription)
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}
