package com.mw.beam.beamwallet.screens.transaction_details

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.views.addDoubleDots
import kotlinx.android.synthetic.main.share_transaction_detail_layout.view.*

class ShareTransactionDetailsView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.share_transaction_detail_layout, this, false)
        addView(view)
    }


    @SuppressLint("SetTextI18n")
    fun setFieldsFromTxDescription(txDescription: TxDescription?) {
        if (txDescription == null) return

        transaction_id.addDoubleDots()
        transaction_fee.addDoubleDots()
        
        if (txDescription.sender.value) {
            my_address_value.text = txDescription.myId
            contact_value.text = txDescription.peerId

            if (txDescription.selfTx) {
                my_address.text = "${context.getString(R.string.my_sending_address)}:"
                contact.text = "${context.getString(R.string.my_receiving_address)}:"
            } else {
                my_address.text = "${context.getString(R.string.my_address)}:"
                contact.text = "${context.getString(R.string.contact)}:"
            }
        } else {
            my_address.text = "${context.getString(R.string.contact)}:"
            contact.text = "${context.getString(R.string.my_address)}:"
            my_address_value.text = txDescription.peerId
            contact_value.text = txDescription.myId
        }

        when (txDescription.sender) {
            TxSender.RECEIVED -> txCurrency.setImageResource(R.drawable.currency_beam_receive)
            TxSender.SENT -> txCurrency.setImageResource(R.drawable.currency_beam_send)
        }

        val createTime = CalendarUtils.fromTimestamp(txDescription.createTime)

        txDate.text = createTime
        confirming_state_text.text = txDescription.getStatusString(context).capitalize()
        amount.text = txDescription.amount.convertToBeamString()
        transaction_id_value.text = txDescription.kernelId
        transaction_fee_value.text = txDescription.fee.convertToBeamString()
        initiated_date_value.text = createTime
        latest_confirmation_value.text = CalendarUtils.fromTimestamp(txDescription.modifyTime)


        imageView.setImageDrawable(txDescription.statusImage())
        confirming_state_text.setTextColor(txDescription.statusColor)
        amount.setTextColor(txDescription.amountColor)

        refreshDrawableState()
        requestLayout()
    }


}