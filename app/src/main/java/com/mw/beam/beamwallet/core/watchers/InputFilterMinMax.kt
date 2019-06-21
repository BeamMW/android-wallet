package com.mw.beam.beamwallet.core.watchers

import android.text.InputFilter
import android.text.Spanned



class InputFilterMinMax(private val min: Int, private val max: Int): InputFilter {

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        var newVal = (dest?.toString()?.substring(0, dstart) ?: "") + (dest?.toString()?.substring(dend, dest.toString().length) ?: "")

        newVal = newVal.substring(0, dstart) + source?.toString() + newVal.substring(dstart, newVal.length)

        return when (newVal.toIntOrNull()) {
            null -> ""
            in min..max -> null
            else -> ""
        }
    }
}