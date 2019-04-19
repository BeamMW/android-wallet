package com.mw.beam.beamwallet.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.AlarmManagerCompat
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
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