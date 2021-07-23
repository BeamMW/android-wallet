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
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import java.io.File

/**
 *  1/21/19.
 */
interface SettingsContract {
    interface View : MvpView {
        fun onNeedAddedViews()
        fun mode(): SettingsFragmentMode
        fun setRunOnRandomNode(runOnRandomNode: Boolean)
        fun setMobileNodeEnabled(enabled: Boolean)
        fun setRunOnBackground(allow: Boolean)
        fun sendMailWithLogs()
        fun setLanguage(language: LocaleHelper.SupportedLanguage)
        fun changePass()
        fun showLockScreenSettingsDialog()
        fun showFingerprintSettings(isFingerprintEnabled: Boolean)
        fun getContext(): Context?
        fun closeDialog()
        fun showMaxPrivacySettingsDialog()
        fun updateLockScreenValue(millis: Long)
        fun updateMaxPrivacyValue(hours: Long)
        fun updateConfirmTransactionValue(isConfirm: Boolean)
        fun showConfirmPasswordDialog(onConfirm: () -> Unit, onDismiss: () -> Unit)
        fun showNodeAddressDialog(nodeAddress: String?)
        fun showInvalidNodeAddressError()
        fun clearInvalidNodeAddressError()
        fun showClearDataDialog()
        fun setAllowOpenExternalLinkValue(allowOpen: Boolean)
        fun navigateToLanguage()
        fun navigateToCurrency()
        fun navigateToOwnerKeyVerification()
        fun navigateToPaymentProof()
        fun showClearDataAlert(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean)
        fun setLogSettings(days:Long)
        fun setCurrencySettings(currency: Currency)
        fun showLogsDialog()
        fun navigateToSeed()
        fun navigateToSeedVerification()
        fun showReceiveFaucet()
        fun onFaucetAddressGenerated(link:String)
        fun showExportDialog()
        fun showExportSaveDialog()
        fun exportSave(content:String)
        fun exportShare(file: File)
        fun showImportDialog()
        fun showConfirmRemoveWallet()
        fun walletRemoved()
        fun exportError()
        fun setAllowNews(allow: Boolean)
        fun setAllowTransaction(allow: Boolean)
        fun setAllowWalletUpdates(allow: Boolean)
        fun setAllowAddressExpiration(allow: Boolean)
        fun onReconnected()
        fun showPublicOfflineAddress()
        fun showRescanDialog()
        fun showUTXO()
    }

    interface Presenter : MvpPresenter<View> {
        fun onReportProblem()
        fun onChangePass()
        fun onShowLockScreenSettings()
        fun onChangeLockSettings(millis: Long)
        fun onDialogClosePressed()
        fun onChangeConfirmTransactionSettings(isConfirm: Boolean)
        fun onChangeFingerprintSettings(isEnabled: Boolean)
        fun onChangeRunOnRandomNode(isEnabled: Boolean)
        fun onChangeRunOnBackground(allow: Boolean)
        fun onChangeAllowOpenExternalLink(allowOpen: Boolean)
        fun onNodeAddressPressed()
        fun onChangeNodeAddress()
        fun onClearDataPressed()
        fun onDialogClearDataPressed(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean)
        fun onConfirmClearDataPressed(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean)
        fun onSaveNodeAddress(address: String?)
        fun onLanguagePressed()
        fun onShowOwnerKey()
        fun onChangeLogSettings(days:Long)
        fun onLogsPressed()
        fun onSeedPressed()
        fun onShowMaxPrivacySettings()
        fun onSeedVerificationPressed()
        fun onReceiveFaucet()
        fun generateFaucetAddress()
        fun onProofPressed()
        fun onExportPressed()
        fun onExportWithExclude(list:Array<String>)
        fun onExportSave()
        fun onRescanPressed()
        fun onExportShare()
        fun omImportPressed()
        fun onRemoveWalletPressed()
        fun onConfirmRemoveWallet()
        fun onCurrencyPressed()
        fun onChangeAllowNews(allow: Boolean)
        fun onChangeAllowTransactionStatus(allow: Boolean)
        fun onChangeAllowWalletUpdates(allow: Boolean)
        fun onChangeAllowAddressExpiration(allow: Boolean)
        fun onShowPublicOfflineAddressPressed()
        fun onChangeMaxPrivacySettings(value: Long)
        fun onEnableMobileNode(enable: Boolean)
        fun onUTXOPressed()
    }

    interface Repository : MvpRepository {
        fun getLockScreenValue(): Long
        fun saveLockSettings(millis: Long)
        fun saveConfirmTransactionSettings(shouldConfirm: Boolean)
        fun saveEnableFingerprintSettings(isEnabled: Boolean)
        fun shouldConfirmTransaction(): Boolean
        fun isFingerPrintEnabled(): Boolean
        fun isAllowOpenExternalLink(): Boolean
        fun isAllowBackgroundMode(): Boolean
        fun setAllowOpenExternalLink(allowOpen: Boolean)
        fun setCurrencySettings(currency: Currency)
        fun setNodeAddress(address: String)
        fun getSavedNodeAddress(): String?
        fun setRunOnRandomNode(random: Boolean)
        fun setRunOnBackground(allow: Boolean)
        fun getCurrentNodeAddress(): String
        fun deleteAddress(addressId: String)
        fun deleteTransaction(txDescription: TxDescription?)
        fun getCurrentLanguage(): LocaleHelper.SupportedLanguage
        fun saveLogSettings(days:Long)
        fun getLogSettings():Long
        fun getDataFile(content:String): File
        fun getCurrencySettings():Currency
        fun isAllowWalletUpdates():Boolean
        fun isAllowTransactions():Boolean
        fun isAllowNews():Boolean
        fun isAllowAddressExpiration():Boolean
        fun setAllowWalletUpdates(allow: Boolean)
        fun setAllowTransactions(allow: Boolean)
        fun setAllowNews(allow: Boolean)
        fun setAllowAddressExpiration(allow: Boolean)
        fun setMobileNodeEnabled(enabled: Boolean)
    }
}
