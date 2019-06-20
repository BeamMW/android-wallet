package com.mw.beam.beamwallet.core.views

import android.annotation.SuppressLint
import android.widget.TextView

@SuppressLint("SetTextI18n")
fun TextView.addDoubleDots() {
    if (!text.toString().endsWith(":")) {
        text = "$text:"
    }
}