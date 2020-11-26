package com.mw.beam.beamwallet.core.helpers

import android.content.Context
import com.mw.beam.beamwallet.R


fun Int.toTimeFormat(context: Context?): String {
    val minutes:Int = (this/60)%60
    val hours:Int = this/3600
    val seconds:Int = this%60

    return when {
        hours>0 && minutes == 0 -> String.format("%d %s", hours, context?.getString(R.string.h))
        hours>0 && minutes > 0 -> String.format("%d %s %d %s", hours, context?.getString(R.string.h), minutes, context?.getString(R.string.m))
        minutes>0 -> String.format("%d %s %d %s", minutes, context?.getString(R.string.m), seconds, context?.getString(R.string.s))
        else -> String.format("%d %s", seconds, context?.getString(R.string.s))
    }
}

fun String.trimAddress(): String {
    return if(this.length <= 12) {
        this
    } else {
        val start = this.substring(0, 6)
        val end = this.substring(this.length-7, this.length)
        "$start...$end"
    }
}