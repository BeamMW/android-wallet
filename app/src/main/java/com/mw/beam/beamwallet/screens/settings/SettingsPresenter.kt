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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.core.helpers.TxStatus
import io.reactivex.disposables.Disposable
import java.net.URI

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsPresenter(currentView: SettingsContract.View, currentRepository: SettingsContract.Repository, private val state: SettingsState)
    : BasePresenter<SettingsContract.View, SettingsContract.Repository>(currentView, currentRepository),
        SettingsContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.isEnabledConnectToRandomNode())
        view?.updateLockScreenValue(repository.getLockScreenValue())
        updateConfirmTransactionValue()
        updateFingerprintValue()
        view?.setAllowOpenExternalLinkValue(repository.isAllowOpenExternalLink())
    }

    override fun onStart() {
        super.onStart()
        view?.updateCategoryList(repository.getAllCategory())
        view?.setLanguage(repository.getCurrentLanguage())
    }

    override fun onAddCategoryPressed() {
        view?.navigateToAddCategory()
    }

    override fun onCategoryPressed(categoryId: String) {
        view?.navigateToCategory(categoryId)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            if (it.own) {
                state.addresses = it.addresses ?: listOf()
            } else {
                state.contacts = it.addresses ?: listOf()
            }
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            val transactions = data.tx?.filter {
                TxStatus.Failed == it.status || TxStatus.Completed == it.status || TxStatus.Cancelled == it.status
            }?.toList()

            state.updateTransactions(transactions)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, txStatusSubscription)


    private fun updateConfirmTransactionValue() {
        view?.updateConfirmTransactionValue(repository.shouldConfirmTransaction())
    }

    private fun updateFingerprintValue() {
        if (FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext()
                        ?: return)) {
            view?.showFingerprintSettings(repository.isFingerPrintEnabled())
        } else {
            repository.saveEnableFingerprintSettings(false)
        }
    }

    override fun onReportProblem() {
        view?.sendMailWithLogs()
    }

    override fun onChangePass() {
        view?.changePass()
    }

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true

    override fun onShowLockScreenSettings() {
        view?.showLockScreenSettingsDialog()
    }

    override fun onChangeLockSettings(millis: Long) {
        repository.saveLockSettings(millis)

        view?.apply {
            updateLockScreenValue(repository.getLockScreenValue())
            closeDialog()
        }
    }

    override fun onChangeConfirmTransactionSettings(isConfirm: Boolean) {
        if (isConfirm) {
            repository.saveConfirmTransactionSettings(isConfirm)
        } else {
            view?.showConfirmPasswordDialog({
                repository.saveConfirmTransactionSettings(isConfirm)
            }, ::updateConfirmTransactionValue)
        }
    }

    override fun onChangeFingerprintSettings(isEnabled: Boolean) {
        if (isEnabled) {
            repository.saveEnableFingerprintSettings(isEnabled)
        } else {
            view?.showConfirmPasswordDialog({
                repository.saveEnableFingerprintSettings(isEnabled)
            }, ::updateFingerprintValue)
        }
    }

    override fun onChangeAllowOpenExternalLink(allowOpen: Boolean) {
        repository.setAllowOpenExternalLink(allowOpen)
    }

    override fun onChangeNodeAddress() {
        view?.clearInvalidNodeAddressError()
    }

    override fun onNodeAddressPressed() {
        if (!repository.isEnabledConnectToRandomNode()) {
            view?.showNodeAddressDialog(repository.getCurrentNodeAddress())
        }
    }

    override fun onLanguagePressed() {
        view?.navigateToLanguage()
    }

    override fun onClearDataPressed() {
        view?.showClearDataDialog()
    }

    override fun onDialogClearDataPressed(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean) {
        view?.closeDialog()
        if (clearAddresses || clearContacts || clearTransactions) {
            view?.showClearDataAlert(clearAddresses, clearContacts, clearTransactions)
        }
    }

    override fun onConfirmClearDataPressed(clearAddresses: Boolean, clearContacts: Boolean, clearTransactions: Boolean) {
        if (clearAddresses) {
            state.addresses.forEach { repository.deleteAddress(it.walletID) }
        }

        if (clearContacts) {
            state.contacts.forEach { repository.deleteAddress(it.walletID) }
        }

        if (clearTransactions) {
            state.transactions.values.forEach { repository.deleteTransaction(it) }
        }
    }

    override fun onChangeRunOnRandomNode(isEnabled: Boolean) {
        if (isEnabled == repository.isEnabledConnectToRandomNode()) {
            return
        }

        if (isEnabled) {
            repository.setRunOnRandomNode(isEnabled)
            view?.init(isEnabled)
            return
        }

        val savedAddress = repository.getSavedNodeAddress()

        if (!savedAddress.isNullOrBlank() && isValidNodeAddress(savedAddress)) {
            repository.setNodeAddress(savedAddress)
            repository.setRunOnRandomNode(isEnabled)
            view?.init(isEnabled)
        } else {
            view?.init(true)
            view?.showNodeAddressDialog(repository.getCurrentNodeAddress())
        }
    }

    override fun onSaveNodeAddress(address: String?) {
        if (!address.isNullOrBlank() && isValidNodeAddress(address)) {
            view?.closeDialog()
            repository.setNodeAddress(address)
            repository.setRunOnRandomNode(false)
            view?.init(false)
        } else {
            view?.showInvalidNodeAddressError()
        }
    }

    private fun isValidNodeAddress(address: String): Boolean {
        return try {
            val uri = URI(QrHelper.BEAM_URI_PREFIX + address)
            !uri.host.isNullOrBlank() && uri.port > 0 && uri.port <= 65535
        } catch (e: Exception) {
            false
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
