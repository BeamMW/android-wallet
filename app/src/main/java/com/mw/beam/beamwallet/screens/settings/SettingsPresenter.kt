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

package com.mw.beam.beamwallet.screens.settings

import android.content.Context
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.utils.LockScreenManager
import java.util.concurrent.TimeUnit

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsPresenter(currentView: SettingsContract.View, currentRepository: SettingsContract.Repository)
    : BasePresenter<SettingsContract.View, SettingsContract.Repository>(currentView, currentRepository),
        SettingsContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
        view?.getContext()?.let {
            view?.updateLockScreenValue(getLockScreenStringIdValue(it))
        }
        val isConfirmTransaction = PreferencesManager.getBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED)
        view?.updateConfirmTransactionValue(isConfirmTransaction)
    }

    override fun onReportProblem() {
        view?.sendMailWithLogs()
    }

    override fun onChangePass() {
        view?.changePass()
    }

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true

    override fun showLockScreenSettings() {
        view?.showLockScreenSettingsDialog()
    }

    override fun onChangeLockSettings(context: Context, settingsId: Int) {
        val time = when (settingsId) {
            R.id.lockNever -> LockScreenManager.LOCK_SCREEN_NEVER_VALUE
            R.id.lockAfter15sec -> TimeUnit.SECONDS.toMillis(15)
            R.id.lockAfter1m -> TimeUnit.MINUTES.toMillis(1)
            R.id.lockAfter5m -> TimeUnit.MINUTES.toMillis(5)
            R.id.lockAfter10m -> TimeUnit.MINUTES.toMillis(10)
            R.id.lockAfter30m -> TimeUnit.MINUTES.toMillis(30)
            else -> LockScreenManager.LOCK_SCREEN_NEVER_VALUE
        }
        LockScreenManager.updateLockScreenSettings(context, time)
        view?.apply {
            updateLockScreenValue(getLockScreenStringIdValue(context))
            closeDialog()
        }
    }

    override fun onChangeConfirmTransactionSettings(isConfirm: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED, isConfirm)
    }

    override fun getLockScreenStringIdValue(context: Context): Int {
        val time = LockScreenManager.getCurrentValue(context)
        return when (time) {
            LockScreenManager.LOCK_SCREEN_NEVER_VALUE -> R.string.never
            TimeUnit.SECONDS.toMillis(15) -> R.string.after_15_seconds
            TimeUnit.MINUTES.toMillis(1) -> R.string.after_1_minute
            TimeUnit.MINUTES.toMillis(5) -> R.string.after_5_minutes
            TimeUnit.MINUTES.toMillis(10) -> R.string.after_10_minutes
            TimeUnit.MINUTES.toMillis(30) -> R.string.after_30_minutes
            else -> R.string.never
        }
    }

    override fun onDialogClosePressed() {
        view?.closeDialog()
    }

    override fun onDestroy() {
        view?.closeDialog()
        super.onDestroy()
    }
}
