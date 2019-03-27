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
import com.mw.beam.beamwallet.core.utils.Consts
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
            R.id.lockNever -> Consts.Preferences.LOCK_SCREEN_NEVER_VALUE
            R.id.lockAfter15sec -> TimeUnit.SECONDS.toMillis(15)
            R.id.lockAfter1m -> TimeUnit.MINUTES.toMillis(1)
            R.id.lockAfter5m -> TimeUnit.MINUTES.toMillis(5)
            R.id.lockAfter10m -> TimeUnit.MINUTES.toMillis(10)
            R.id.lockAfter30m -> TimeUnit.MINUTES.toMillis(30)
            else -> Consts.Preferences.LOCK_SCREEN_NEVER_VALUE
        }
        LockScreenManager.updateLockScreenSettings(context, time)
        view?.updateLockScreenValue()
    }

    override fun onDialogClosePressed() {
        view?.closeDialog()
    }
}
