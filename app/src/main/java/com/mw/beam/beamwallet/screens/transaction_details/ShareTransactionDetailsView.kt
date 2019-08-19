package com.mw.beam.beamwallet.screens.transaction_details

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxFailureReason
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
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

        val createTime = CalendarUtils.fromTimestamp(txDescription.createTime)

        txDate.text = createTime
        confirming_state_text.text = txDescription.getStatusString(context).capitalize()
        amount.text = txDescription.amount.convertToBeamString()
        transaction_id_value.text = txDescription.kernelId
        transaction_fee_value.text = txDescription.fee.convertToBeamString()
        initiated_date_value.text = createTime
        latest_confirmation_value.text = CalendarUtils.fromTimestamp(txDescription.modifyTime)

        when (txDescription.status) {
            TxStatus.InProgress, TxStatus.Pending -> {
                when (txDescription.sender) {
                    TxSender.RECEIVED -> {
                        setColors(R.color.received_color)
                        setDrawables(R.drawable.ic_icon_receive, R.drawable.currency_beam_receive)
                    }
                    TxSender.SENT -> {
                        setColors(R.color.sent_color)
                        setDrawables(R.drawable.ic_sending_share_transaction_details, R.drawable.currency_beam_send)
                    }
                }
            }
            TxStatus.Registered -> {
                when {
                    TxSender.RECEIVED == txDescription.sender -> {
                        setColors(R.color.received_color)
                        setDrawables(R.drawable.ic_icon_receive, R.drawable.currency_beam_receive)
                    }
                    TxSender.SENT == txDescription.sender && txDescription.selfTx -> {
                        setColors(R.color.common_text_color)
                        setDrawables(R.drawable.ic_icon_sending_own, R.drawable.currency_beam)
                    }
                    TxSender.SENT == txDescription.sender -> {
                        setColors(R.color.sent_color)
                        setDrawables(R.drawable.ic_sending_share_transaction_details, R.drawable.currency_beam_send)
                    }
                    else -> {}
                }
            }
            TxStatus.Completed -> {
                if (txDescription.selfTx) {
                    setColors(R.color.common_text_color)
                    setDrawables(R.drawable.ic_icon_sent_own, R.drawable.currency_beam)
                } else {
                    when (txDescription.sender) {
                        TxSender.RECEIVED -> {
                            setColors(R.color.received_color)
                            setDrawables(R.drawable.ic_icon_received, R.drawable.currency_beam_receive)
                        }
                        TxSender.SENT -> {
                            setColors(R.color.sent_color)
                            setDrawables(R.drawable.ic_icon_sent, R.drawable.currency_beam_send)
                        }
                    }
                }
            }
            TxStatus.Cancelled -> {
                setColors(R.color.common_text_dark_color)
                setDrawables(R.drawable.ic_icon_canceled, R.drawable.currency_beam)
            }
            TxStatus.Failed -> {
                when (txDescription.failureReason) {
                    TxFailureReason.TRANSACTION_EXPIRED -> {
                        setColors(R.color.common_text_dark_color)
                        setDrawables(R.drawable.ic_expired, R.drawable.currency_beam)
                    }
                    else -> {
                        setColors(R.color.common_text_dark_color)
                        setDrawables(R.drawable.ic_icon_canceled, R.drawable.currency_beam)
                    }
                }
            }
        }

        refreshDrawableState()
        requestLayout()
    }

    private fun setDrawables(iconId: Int, currencyId: Int) {
        imageView.setImageDrawable(ContextCompat.getDrawable(context, iconId))
        txCurrency.setImageDrawable(ContextCompat.getDrawable(context, currencyId))
    }

    private fun setColors(colorId: Int) {
        val color = ContextCompat.getColor(context, colorId)
        amount.setTextColor(color)
        confirming_state_text.setTextColor(color)
    }

}