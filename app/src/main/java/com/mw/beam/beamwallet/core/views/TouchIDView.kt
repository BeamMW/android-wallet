package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import kotlinx.android.synthetic.main.touchid_view.view.*
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator

enum class Type(val value: Int) {
    ERROR(0),
    FAILED(1),
    NORMAL(2),
    SUCCESS(3)
}

class TouchIDView : LinearLayout {

    private var delayedTask: DelayedTask? = null

    fun setType(type:Type) {
        if (type == Type.ERROR) {
            showError()
        }
        else if (type == Type.FAILED) {
            showFailed()
        }
        else if (type == Type.SUCCESS) {
            showSuccess()
        }
        else{
            showNormal()
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
        inflate(context, R.layout.touchid_view, this)

        this.orientation = VERTICAL
        this.gravity = Gravity.CENTER
    }

    private fun showSuccess() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_success, resources.getColor(R.color.fingerprint_card_background_color_success,null), false)
    }

    private fun showNormal() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch, resources.getColor(R.color.fingerprint_card_background_color,null), false)
    }

    private fun showFailed() {
        animatedChangeDrawable(R.drawable.ic_touch_error, resources.getColor(R.color.fingerprint_card_background_color_error,null), true)

        delayedTask?.cancel(true)
        delayedTask = DelayedTask.startNew(2, {
            animatedChangeDrawable(R.drawable.ic_touch, resources.getColor(R.color.fingerprint_card_background_color,null), false)
        })
    }

    private fun showError() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_error, resources.getColor(R.color.fingerprint_card_background_color_error,null), false)
    }

    private fun animatedChangeDrawable(resId: Int, resColor:Int, visible:Boolean) {
        fingerprintImage.setImageDrawable(context?.getDrawable(resId))

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
