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
 *  4/2/19.
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
    const val KEY_CONNECT_TO_RANDOM_NODE = "KEY_CONNECT_TO_RANDOM_NODE"
    const val KEY_NODE_ADDRESS = "KEY_NODE_ADDRESS"
    const val KEY_ALWAYS_OPEN_LINK = "KEY_ALWAYS_OPEN_LINK"
    const val KEY_TAG_DATA = "KEY_TAG_DATA"
    const val KEY_LANGUAGE_CODE = "KEY_LANGUAGE_CODE_NEW"
    const val KEY_UNFINISHED_RESTORE = "KEY_UNFINISHED_RESTORE"
    const val KEY_RESTORED_FROM_TRUSTED = "KEY_RESTORED_FROM_TRUSTED"
    const val KEY_LOGS = "KEY_LOGS"
    const val KEY_DEFAULT_LOGS = "KEY_DEFAULT_LOGS"
    const val KEY_SEED = "KEY_SEED"
    const val KEY_SEED_IS_SKIP = "KEY_SEED_IS_SKIP"
    const val KEY_TAG_DATA_LEGACY = "KEY_TAG_DATA_LEGACY"
    const val DARK_MODE = "DARK_MODE"
    const val DARK_MODE_DEFAULT = "DARK_MODE_DEFAULT"
    const val KEY_BACKGROUND_MODE = "KEY_BACKGROUND_MODE"
    const val KEY_BACKGROUND_MODE_ASK = "KEY_BACKGROUND_MODE_ASK"
    const val KEY_TAG_DATA_RECOVER = "KEY_TAG_DATA_RECOVER"
    const val KEY_CURRENCY = "KEY_CURRENCY"
   // const val KEY_CURRENCY_RECOVER = "KEY_CURRENCY_RECOVER"
    const val KEY_WALLET_UPDATES = "KEY_WALLET_UPDATES"
    const val KEY_TRANSACTIONS_STATUS = "KEY_TRANSACTIONS_STATUS"
    const val KEY_NEWS = "KEY_NEWS"
    const val KEY_ADDRESS_EXPIRATION = "KEY_ADDRESS_EXPIRATION"
    const val IGNORE_CONTACTS = "IGNORE_CONTACTS"

    const val KEY_TRANSACTIONS = "KEY_TRANSACTIONS"
    const val KEY_NOTIFICATIONS = "KEY_NOTIFICATIONS"
    const val KEY_WALLET_STATUS = "KEY_WALLET_STATUS"
    const val KEY_ADDRESSES = "KEY_ADDRESSES"

    fun putString(key: String, value: String) = preferenceStore.edit().putString(key, value).apply()
    fun getString(key: String): String? = preferenceStore.getString(key, null)
    fun putBoolean(key: String, value: Boolean) = preferenceStore.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, defValue: Boolean = false): Boolean = preferenceStore.getBoolean(key, defValue)
    fun putLong(key: String, value: Long) = preferenceStore.edit().putLong(key, value).apply()
    fun getLong(key: String, defValue: Long = 0L) = preferenceStore.getLong(key, defValue)

    fun clear() {
        putString(KEY_PASSWORD,"");
        putString(KEY_SEED,"");
        putBoolean(KEY_IS_SENDING_CONFIRM_ENABLED,false)
        putBoolean(KEY_IS_FINGERPRINT_ENABLED,false)
        putLong(KEY_LOCK_SCREEN, 0L)
        putBoolean(KEY_PRIVACY_MODE,false)
        putBoolean(KEY_PRIVACY_MODE_NEED_CONFIRM,false)
        putBoolean(KEY_CONNECT_TO_RANDOM_NODE,true)
        putBoolean(KEY_ALWAYS_OPEN_LINK,false)
        putString(KEY_TAG_DATA,"");
        putBoolean(KEY_SEED_IS_SKIP,true)
        putBoolean(KEY_WALLET_UPDATES,true)
        putBoolean(KEY_TRANSACTIONS_STATUS,true)
        putBoolean(KEY_NEWS,true)
        putBoolean(KEY_ADDRESS_EXPIRATION,true)
    }
}