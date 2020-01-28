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


import android.content.Context
import com.mw.beam.beamwallet.core.App
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import java.util.Timer
import kotlin.concurrent.timerTask

object LockScreenManager {
    var isShowedLockScreen = false

    var subOnStatusLock: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()

    const val LOCK_SCREEN_NEVER_VALUE = 0L

    private var timer:Timer? = null
    var inactiveDate = 0L

    fun checkIsNeedShow():Boolean {
        if (inactiveDate > 0L) {
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - inactiveDate
            val time = getCurrentValue()

            if (time > 0 && diff >= time) {
                isShowedLockScreen = true
                subOnStatusLock.onNext(0)
            }
        }
        return  false
    }

    fun restartTimer(context: Context) {
        timer?.cancel()
        timer = null

        val time = getCurrentValue()

        if (time > 0) {
            timer = Timer()
            timer?.schedule(timerTask {
                isShowedLockScreen = true
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