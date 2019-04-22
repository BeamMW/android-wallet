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

package com.mw.beam.beamwallet.core.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.AlarmManagerCompat
import java.util.concurrent.TimeUnit

object LockScreenManager {
    private const val REQUEST_CODE = 721

    const val LOCK_SCREEN_ACTION = "com.mw.beam.beamwallet.core.utils.LockScreen"
    const val LOCK_SCREEN_NEVER_VALUE = 0L

    fun restartTimer(context: Context) {
        val lockScreenIntent = Intent(LOCK_SCREEN_ACTION)
        val lockScreenPendingIntent = PendingIntent
                .getBroadcast(context, REQUEST_CODE, lockScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val time = getCurrentValue()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(lockScreenPendingIntent)
        if (time > 0) {
            val alarmTime = System.currentTimeMillis() + time
            AlarmManagerCompat.setExact(alarmManager, AlarmManager.RTC_WAKEUP, alarmTime, lockScreenPendingIntent)
        }
    }

    fun getCurrentValue(): Long = PreferencesManager.getLong(PreferencesManager.KEY_LOCK_SCREEN, LOCK_SCREEN_NEVER_VALUE)

    fun updateLockScreenSettings(context: Context, millisecond: Long) {
        PreferencesManager.putLong(PreferencesManager.KEY_LOCK_SCREEN, millisecond)
        restartTimer(context)
    }
}

fun Long.isLessMinute() = this < TimeUnit.MINUTES.toMillis(1)
