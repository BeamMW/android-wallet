package com.mw.beam.beamwallet.wallet

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.EntitiesHelper
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*

/**
 * Created by vain onnellinen on 10/2/18.
 */
class TransactionsAdapter(private val context: Context, private var data: List<TxDescription>, private val clickListener: OnItemClickListener) :
        RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private val beamResId = R.drawable.ic_beam
    private val sentCurrencyResId = R.drawable.beam_sent
    private val receivedCurrencyResId = R.drawable.beam_received
    private val receivedColor = ContextCompat.getColor(context, R.color.received_color)
    private val receivedStatus = context.getString(R.string.wallet_status_received)
    private val sentColor = ContextCompat.getColor(context, R.color.sent_color)
    private val sentStatus = context.getString(R.string.wallet_status_sent)
    private val commonStatusColor = ContextCompat.getColor(context, R.color.transaction_common_status_color)
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
                EntitiesHelper.TxSender.RECEIVED -> {
                    sum.setTextColor(receivedColor)
                    status.setTextColor(receivedColor)
                    status.text = receivedStatus
                    currency.setImageResource(receivedCurrencyResId)
                    message.text = String.format(receiveText, "BEAM") //TODO replace when multiply currency will be available
                }
                EntitiesHelper.TxSender.SENT -> {
                    sum.setTextColor(sentColor)
                    status.setTextColor(sentColor)
                    status.text = sentStatus
                    currency.setImageResource(sentCurrencyResId)
                    message.text = String.format(sendText, "BEAM") //TODO replace when multiply currency will be available
                }
            }

            if (transaction.statusEnum == EntitiesHelper.TxStatus.Failed
                    || transaction.statusEnum == EntitiesHelper.TxStatus.InProgress
                    || transaction.statusEnum == EntitiesHelper.TxStatus.Cancelled) {
                status.setTextColor(commonStatusColor)
                status.text = transaction.statusEnum.name.toLowerCase() //TODO make resources when all statuses will be stable
            }

            itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor)
            icon.setImageResource(beamResId)
            date.text = CalendarUtils.fromTimestamp(transaction.modifyTime * 1000)
            sum.text = EntitiesHelper.convertToBeamWithSign(transaction.amount, transaction.sender)
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
