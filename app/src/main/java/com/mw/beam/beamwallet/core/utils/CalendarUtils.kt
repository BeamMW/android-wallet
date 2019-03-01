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
 * Created by vain onnellinen on 10/4/18.
 */
object CalendarUtils {
    private val WALLET_TIME_FORMAT = SimpleDateFormat("d MMM yyyy  |  HH:mm a", AppConfig.LOCALE)

    fun fromTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return WALLET_TIME_FORMAT.format(calendar.time)
    }
}

fun Long.isBefore(): Boolean {
    val calendar = Calendar.getInstance()
    val currentCalendar = Calendar.getInstance()
    calendar.timeInMillis = this

    return currentCalendar.after(calendar)
}
