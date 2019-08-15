package com.mw.beam.beamwallet.screens.transaction_details

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import kotlinx.android.synthetic.main.share_transaction_detail_layout.view.*

class ShareTransactionDetailsView : FrameLayout {

    constructor(context: Context?) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    init {
        var view = LayoutInflater.from(context).inflate(R.layout.share_transaction_detail_layout, this, false)
        addView(view)
    }


    fun setFieldsFromTxDescription(txDescription: TxDescription?) {
        date.text = txDescription?.createTime.toString()
        amount.text = txDescription?.amount.toString()
        contact_value.text = txDescription?.peerId
        my_address_value.text = txDescription?.myId
        transaction_id_value.text = txDescription?.id
        transaction_fee_value.text = txDescription?.fee.toString()
        initiated_date_value.text = txDescription?.createTime.toString()
        latest_confirmation_value.text = txDescription?.modifyTime.toString()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var desiredWidth = 1080
        var desiredHeight = 1920

        var width = 0
        var height = 0

        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST){
            width = Math.min(desiredWidth, widthSize)
            height = Math.min(desiredHeight, heightSize)
        } else {
            width = desiredWidth
            height = desiredHeight
        }

        if (width > 0 && height > 0){
            setMeasuredDimension(width, height)
        }
    }

    fun getTxId():String{return transaction_id_value.text.toString()
    }

}