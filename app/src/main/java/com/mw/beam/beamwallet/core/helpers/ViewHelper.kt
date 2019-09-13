package com.mw.beam.beamwallet.core.helpers

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import com.mw.beam.beamwallet.R

fun View.selector(idle: Int = R.color.colorPrimary, pressed: Int = R.color.black_02) {
    val res = StateListDrawable()
    res.setExitFadeDuration(300)
    res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(resources.getColor(pressed)))
    res.addState(intArrayOf(), ColorDrawable(resources.getColor(idle)))
    background = res
}