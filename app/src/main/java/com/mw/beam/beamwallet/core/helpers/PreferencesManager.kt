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

import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.utils.LogUtils
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore

/**
 * Created by vain onnellinen on 4/2/19.
 */
object PreferencesManager {
    private val preferenceStore: SecuredPreferenceStore by lazy {
        try {
            SecuredPreferenceStore.init(App.self, null, null, null, DefaultRecoveryHandler())
        } catch (expected: Throwable) {
            LogUtils.log("PreferencesManager init failed: $expected")
            //TODO should we throw some custom exception here?
        }

        SecuredPreferenceStore.getSharedInstance()
    }

    const val KEY_IS_SENDING_CONFIRM_ENABLED = "KEY_IS_SENDING_CONFIRM_ENABLED"
    const val KEY_IS_FINGERPRINT_ENABLED = "KEY_IS_FINGERPRINT_ENABLED"
    const val KEY_PASSWORD = "KEY_PASSWORD"
    const val KEY_LOCK_SCREEN = "KEY_LOCK_SCREEN"
    const val KEY_PRIVACY_MODE = "KEY_PRIVACY_MODE"
    const val KEY_PRIVACY_MODE_NEED_CONFIRM = "KEY_PRIVACY_MODE_NEED_CONFIRM"

    fun putString(key: String, value: String) = preferenceStore.edit().putString(key, value).apply()
    fun getString(key: String): String? = preferenceStore.getString(key, null)
    fun putBoolean(key: String, value: Boolean) = preferenceStore.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, defValue: Boolean = false): Boolean = preferenceStore.getBoolean(key, defValue)
    fun putLong(key: String, value: Long) = preferenceStore.edit().putLong(key, value).apply()
    fun getLong(key: String, defValue: Long = 0L) = preferenceStore.getLong(key, defValue)
}