package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.biometric_view.view.*


enum class Status(val value: Int) {
    ERROR(0),
    FAILED(1),
    SUCCESS(3),
    CANCEL(4)
}

enum class Type(val value: Int) {
    FINGER(0),
    FACE(1)
}


class BiometricView : LinearLayout {

    private var delayedTask: DelayedTask? = null
    var type = Type.FINGER

    fun setStatus(status:Status) {
        when (status) {
            Status.ERROR -> showError()
            Status.FAILED -> showFailed()
            Status.SUCCESS -> showSuccess()
        }
    }

    fun setBiometricType(type:Type) {
        this.type = type
        when (type) {
            Type.FACE -> biometricImage.setImageDrawable(context?.getDrawable(R.drawable.ic_face_id))
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.biometric_view, this)

        this.orientation = VERTICAL
        this.gravity = Gravity.CENTER
    }

    private fun showSuccess() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.color.common_text_color, resources.getColor(R.color.fingerprint_card_background_color_success,null), false)
    }

    private fun showFailed() {
        when (type) {
            Type.FINGER -> fingerErrorLabel.text = context.getString(R.string.fingerprint_not_recognized)
            else -> fingerErrorLabel.text = context.getString(R.string.faceid_not_recognized)
        }

        animatedChangeDrawable(R.color.common_text_color, resources.getColor(R.color.fingerprint_card_background_color_error,null), true)

        delayedTask?.cancel(true)
        delayedTask = DelayedTask.startNew(2, {
            animatedChangeDrawable(R.color.common_text_color, resources.getColor(R.color.fingerprint_card_background_color,null), false)
        })
    }

    private fun showError() {
        when (type) {
            Type.FINGER -> fingerErrorLabel.text = context.getString(R.string.common_fingerprint_error)
            else -> fingerErrorLabel.text = context.getString(R.string.common_faceid_error)
        }

        delayedTask?.cancel(true)

        animatedChangeDrawable(R.color.common_text_color, resources.getColor(R.color.fingerprint_card_background_color_error,null), true)
    }

    private fun animatedChangeDrawable(iconColor:Int, resColor:Int, visible:Boolean) {
        biometricImage.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, iconColor))

        if(visible) {
            fingerErrorLabel.visible(true)
        }
        else{
            fingerErrorLabel.gone(true)
        }

        val colorFrom = when(resColor==resources.getColor(R.color.fingerprint_card_background_color,null)) {
            true->resources.getColor(R.color.fingerprint_card_background_color_error,null)
            else -> resources.getColor(R.color.fingerprint_card_background_color,null)
        }
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, resColor)
        colorAnimation.duration = 400
        colorAnimation.addUpdateListener { animator -> btnTouch.setCardBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }


}
