package com.mw.beam.beamwallet.core.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import com.mw.beam.beamwallet.R
import kotlin.math.roundToInt


class ScreenHelper {

    companion object {
        fun dpToPx(context:Context?, dp: Int): Int {
            return if(context!=null) {
                val density = context.resources
                        .displayMetrics
                        .density
                (dp.toFloat() * density).roundToInt()
            } else{
                dp
            }
        }

        fun pxToDp(context:Context?, px: Int): Int {
            return if(context!=null) {
                val density = context.resources
                        .displayMetrics
                        .density
                (px.toFloat() / density).roundToInt()

            } else{
                px
            }
        }
    }

}