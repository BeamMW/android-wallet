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
import java.lang.Exception
import java.net.URI

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsPresenter(currentView: SettingsContract.View, currentRepository: SettingsContract.Repository)
    : BasePresenter<SettingsContract.View, SettingsContract.Repository>(currentView, currentRepository),
        SettingsContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.isEnabledConnectToRandomNode())
        view?.updateLockScreenValue(repository.getLockScreenValue())
        updateConfirmTransactionValue()
        updateFingerprintValue()
    }

    private fun updateConfirmTransactionValue() {
        view?.updateConfirmTransactionValue(repository.shouldConfirmTransaction())
    }

    private fun updateFingerprintValue() {
        if (FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext() ?: return)) {
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

    override fun hasBackArrow(): Boolean? = null
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

    override fun onChangeNodeAddress() {
        view?.clearInvalidNodeAddressError()
    }

    override fun onNodeAddressPressed() {
        if (!repository.isEnabledConnectToRandomNode()) {
            view?.showNodeAddressDialog(repository.getCurrentNodeAddress())
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
            !uri.host.isNullOrBlank() && uri.port > 0
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
