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
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

/**
 * Created by vain onnellinen on 1/21/19.
 */
interface SettingsContract {
    interface View : MvpView {
        fun init()
        fun sendMailWithLogs()
        fun changePass()
        fun showLockScreenSettingsDialog()
        fun closeDialog()
        fun getContext(): Context?
        fun updateLockScreenValue(stringResId: Int)
        fun updateConfirmTransactionValue(isConfirm: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onReportProblem()
        fun onChangePass()
        fun showLockScreenSettings()
        fun onChangeLockSettings(context: Context, settingsId: Int)
        fun onDialogClosePressed()
        fun getLockScreenStringIdValue(context: Context): Int
        fun onChangeConfirmTransactionSettings(isConfirm: Boolean)
    }

    interface Repository : MvpRepository
}
