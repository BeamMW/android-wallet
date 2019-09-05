/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.core.utils

import com.mw.beam.beamwallet.core.AppConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 *  10/4/18.
 */
object CalendarUtils {
    private const val TIME_FORMAT = "d MMM yyyy  |  hh:mm a"
    private const val SHORT_TIME_FORMAT = "d MMM"
    private val US_TIME_FORMAT = SimpleDateFormat(TIME_FORMAT, Locale.US)

    fun fromTimestamp(timestamp: Long, dataFormat: SimpleDateFormat = SimpleDateFormat(TIME_FORMAT, AppConfig.LOCALE)): String {
        val calendar = calendarFromTimestamp(timestamp)
        return dataFormat.format(calendar.time)
    }

    fun calendarFromTimestamp(timestamp: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar
    }

    fun fromTimestampUS(timestamp: Long): String {
        val calendar = calendarFromTimestamp(timestamp)
        return US_TIME_FORMAT.format(calendar.time)
    }

    fun fromTimestampShort(timestamp: Long): String {
        val calendar = calendarFromTimestamp(timestamp)
        return SimpleDateFormat(SHORT_TIME_FORMAT, AppConfig.LOCALE).format(calendar.time)
    }
}

fun Long.isBefore(): Boolean {
    val calendar = Calendar.getInstance()
    val currentCalendar = Calendar.getInstance()
    calendar.timeInMillis = this

    return currentCalendar.after(calendar)
}
