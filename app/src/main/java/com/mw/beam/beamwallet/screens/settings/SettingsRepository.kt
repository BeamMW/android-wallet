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
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

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

    override fun saveEnableFingerprintSettings(isEnabled: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED, isEnabled)
    }

    override fun shouldConfirmTransaction(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_SENDING_CONFIRM_ENABLED)
    }

    override fun isFingerPrintEnabled(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED)
    }

    override fun getSavedNodeAddress(): String? {
        return PreferencesManager.getString(PreferencesManager.KEY_NODE_ADDRESS)
    }

    override fun setRunOnRandomNode(random: Boolean) {
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, random)

        if (random) {
            AppConfig.NODE_ADDRESS = Api.getDefaultPeers().random()
            App.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
        }
    }

    override fun deleteTransaction(txDescription: TxDescription?) {
        if (txDescription != null) {
            getResult("deleteTransaction", "kernelID = ${txDescription.kernelId}") {
                wallet?.deleteTx(txDescription.id)
            }
        }
    }

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult(WalletListener.subOnTxStatus, "getTxStatus") {
            wallet?.getWalletStatus()
        }
    }

    override fun deleteAddress(addressId: String) {
        CategoryHelper.changeCategoryForAddress(addressId, null)
        getResult("deleteAddress") {
            wallet?.deleteAddress(addressId)
        }
    }

    override fun getAllCategory(): List<Category> {
        return CategoryHelper.getAllCategory()
    }

    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, "getAddresses") {
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
        }
    }

    override fun getCurrentLanguage(): String {
        return LocaleHelper.currentLanguage
    }

    override fun setNodeAddress(address: String) {
        AppConfig.NODE_ADDRESS = address
        App.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
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
}
