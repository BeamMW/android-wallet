package com.mw.beam.beamwallet.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.AlarmManagerCompat
import java.util.concurrent.TimeUnit

object LockScreenManager {
    const val LOCK_SCREEN_ACTION = "com.mw.beam.beamwallet.core.utils.LockScreen"
    private const val LOCK_SCREEN_KEY = "lock_screen"
    const val LOCK_SCREEN_NEVER_VALUE = 0L

    private const val requestCode = 721

    fun restartTimer(context: Context) {
        val lockScreenIntent = Intent(LOCK_SCREEN_ACTION)
        val lockScreenPendingIntent = PendingIntent
                .getBroadcast(context, requestCode, lockScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val time = getCurrentValue(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(lockScreenPendingIntent)
        if (time > 0) {
            val alarmTime = System.currentTimeMillis() + time
            AlarmManagerCompat.setExact(alarmManager, AlarmManager.RTC_WAKEUP, alarmTime, lockScreenPendingIntent)
        }
    }

    fun getCurrentValue(context: Context): Long {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getLong(LOCK_SCREEN_KEY, LOCK_SCREEN_NEVER_VALUE)
    }

    fun updateLockScreenSettings(context: Context, millisecond: Long) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = pref.edit()
        editor.putLong(LOCK_SCREEN_KEY, millisecond)
        editor.apply()
        restartTimer(context)
    }
}

fun Long.isLessMinute() = this < TimeUnit.MINUTES.toMillis(1)