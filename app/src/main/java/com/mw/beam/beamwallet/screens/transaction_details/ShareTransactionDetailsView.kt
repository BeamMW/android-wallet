package com.mw.beam.beamwallet.screens.transaction_details

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.utils.CalendarUtils
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
    fun configGeneralTransactionInfo(txDescription: TxDescription?) {
        if (txDescription == null) return

        when (txDescription.sender) {
            TxSender.RECEIVED -> txCurrency.setImageResource(R.drawable.currency_beam_receive)
            TxSender.SENT -> txCurrency.setImageResource(R.drawable.currency_beam_send)
        }

        val createTime = CalendarUtils.fromTimestamp(txDescription.createTime)

        txDate.text = createTime
        confirming_state_text.text = txDescription.getStatusString(context).capitalize()
        amount.text = txDescription.amount.convertToBeamString()

        if (txDescription.sender.value) {
            if (txDescription.selfTx) {
                startAddress.text = txDescription.myId
                endAddress.text = txDescription.peerId

                startAddressTitle.text = "${context.getString(R.string.my_sending_address)}".toUpperCase()
                endAddressTitle.text = "${context.getString(R.string.my_receiving_address)}".toUpperCase()
            } else {
                startAddressTitle.text = "${context.getString(R.string.contact)}".toUpperCase()
                endAddressTitle.text = "${context.getString(R.string.my_address)}".toUpperCase()

                startAddress.text = txDescription.peerId
                endAddress.text = txDescription.myId
            }
        }
        else {
            startAddressTitle.text = "${context.getString(R.string.contact)}".toUpperCase()
            endAddressTitle.text = "${context.getString(R.string.my_address)}".toUpperCase()
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        feeLabel.text = txDescription.fee.toString() + " GROTH"
        idLabel.text = txDescription.id
        kernelLabel.text = txDescription.kernelId

        imageView.setImageDrawable(txDescription.statusImage())
        confirming_state_text.setTextColor(txDescription.statusColor)
        amount.setTextColor(txDescription.amountColor)

        refreshDrawableState()
        requestLayout()

        if (txDescription.status == TxStatus.Cancelled || txDescription.status == TxStatus.Failed
                || txDescription.kernelId.contains("000000000")) {
            kernelLayout.visibility = View.GONE
        }
    }
}