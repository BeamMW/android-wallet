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

package com.mw.beam.beamwallet.screens.send_confirmation.dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import java.io.Serializable

interface ConfirmTransactionContract {

    interface View: MvpView {
        fun init(isFingerprintEnable: Boolean)
        fun showFailedFingerprint()
        fun showErrorFingerprint()
        fun showSuccessFingerprint()
        fun showEmptyPasswordError()
        fun showWrongPasswordError()
        fun clearPasswordError()
        fun close(success: Boolean)
    }

    interface Presenter: MvpPresenter<View> {
        fun onCancel()
        fun onSuccessFingerprint()
        fun onFailedFingerprint()
        fun onErrorFingerprint()
        fun onPasswordChanged()
        fun onOkPressed(password: String)
    }

    interface Repository: MvpRepository {
        fun isFingerPrintEnabled(): Boolean
        fun checkPassword(password: String): Boolean
    }

    interface Callback: Serializable {
        fun onClose(success: Boolean)
    }
}