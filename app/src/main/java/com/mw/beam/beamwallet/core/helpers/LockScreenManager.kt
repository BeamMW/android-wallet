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
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import com.mw.beam.beamwallet.core.App
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import java.util.Timer
import kotlin.concurrent.timerTask

object LockScreenManager {
    var subOnStatusLock: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()

    private const val REQUEST_CODE = 721

    const val LOCK_SCREEN_ACTION = "com.mw.beam.beamwallet.core.utils.LockScreen"
    const val LOCK_SCREEN_NEVER_VALUE = 0L

    private var timer:Timer? = null

    var isNeedLocked = false

    fun restartTimer(context: Context) {
        timer?.cancel()
        timer = null

        Log.e("lockApp","restartTimer")

        val time = getCurrentValue()

        if (time > 0) {
            timer = Timer()
            timer?.schedule(timerTask {
                Log.e("lockApp","timer FIRED")

                isNeedLocked = true

                subOnStatusLock.onNext(0)

            }, time)
        }

    }

    fun getCurrentValue(): Long = PreferencesManager.getLong(PreferencesManager.KEY_LOCK_SCREEN, LOCK_SCREEN_NEVER_VALUE)

    fun updateLockScreenSettings(millisecond: Long) {
        PreferencesManager.putLong(PreferencesManager.KEY_LOCK_SCREEN, millisecond)
        restartTimer(App.self)
    }
}

fun Long.isLessMinute() = this < TimeUnit.MINUTES.toMillis(1)