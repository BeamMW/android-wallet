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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.LockScreenManager

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsRepository : BaseRepository(), SettingsContract.Repository {
    override fun getLockScreenValue(): Long = LockScreenManager.getCurrentValue()

    override fun saveLockSettings(millis: Long) {
        LockScreenManager.updateLockScreenSettings(millis)
    }

    override fun saveConfirmTransactionSettings(shouldConfirm: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED, shouldConfirm)
    }

    override fun shouldConfirmTransaction(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED)
    }
}
