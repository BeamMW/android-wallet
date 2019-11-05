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
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.*
import java.io.File

/**
 *  1/21/19.
 */
class SettingsRepository : BaseRepository(), SettingsContract.Repository {
    override fun getLockScreenValue(): Long = LockScreenManager.getCurrentValue()

    override fun saveLockSettings(millis: Long) {
        LockScreenManager.updateLockScreenSettings(millis)
    }

    override fun saveConfirmTransactionSettings(shouldConfirm: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED, shouldConfirm)
    }

    override fun saveEnableFingerprintSettings(isEnabled: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED, isEnabled)
    }

    override fun shouldConfirmTransaction(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED)
    }

    override fun isFingerPrintEnabled(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED)
    }

    override fun saveLogSettings(days:Long) {
        PreferencesManager.putLong(PreferencesManager.KEY_LOGS,days)
    }

    override fun getLogSettings():Long {
        return PreferencesManager.getLong(PreferencesManager.KEY_LOGS)
    }

    override fun getSavedNodeAddress(): String? {
        return PreferencesManager.getString(PreferencesManager.KEY_NODE_ADDRESS)
    }

    override fun setRunOnRandomNode(random: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, random)

        if (random) {
            AppManager.instance.onChangeNodeAddress()
            AppConfig.NODE_ADDRESS = Api.getDefaultPeers().random()
            AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
        }
    }

    override fun deleteTransaction(txDescription: TxDescription?) {
        if (txDescription != null) {
            getResult("deleteTransaction", "kernelID = ${txDescription.kernelId}") {
                wallet?.deleteTx(txDescription.id)
            }
        }
    }

    override fun deleteAddress(addressId: String) {
        TagHelper.changeTagsForAddress(addressId, null)
        getResult("deleteAddress") {
            wallet?.deleteAddress(addressId)
        }
    }

    override fun getAllCategory() : List<Tag>{
        return TagHelper.getAllTagsSorted()
    }


    override fun getCurrentLanguage(): LocaleHelper.SupportedLanguage {
        return LocaleHelper.getCurrentLanguage()
    }

    override fun setNodeAddress(address: String) {
        AppConfig.NODE_ADDRESS = address
        AppManager.instance.onChangeNodeAddress()
        AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
        PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, address)
    }

    override fun getCurrentNodeAddress(): String {
        return AppConfig.NODE_ADDRESS
    }

    override fun isAllowOpenExternalLink(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)
    }

    override fun setAllowOpenExternalLink(allowOpen: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK, allowOpen)
    }

    override fun getDataFile(content:String): File {
        val file = File(AppConfig.TRANSACTIONS_PATH, "wallet_data_" + System.currentTimeMillis() + ".json")

        if (!file.parentFile.exists()) {
            file.parentFile.mkdir()
        } else {
            file.parentFile.listFiles().forEach { it.delete() }
        }
        file.createNewFile()

        val stringBuilder = StringBuilder()
        stringBuilder.append(content)

        file.writeBytes(stringBuilder.toString().toByteArray())

        return file
    }
}
