package com.mw.beam.beamwallet.core.utils

import com.mw.beam.beamwallet.core.AppConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by vain onnellinen on 10/4/18.
 */
object CalendarUtils {
    private val WALLET_TIME_FORMAT = SimpleDateFormat("dd MMM yyyy  |  HH:mm a", AppConfig.LOCALE)

    fun fromTimestamp(timestamp : Long) : String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return WALLET_TIME_FORMAT.format(calendar.time)
    }
}
