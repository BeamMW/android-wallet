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

package com.mw.beam.beamwallet.screens.settings.password_dialog

import android.os.Handler
import android.text.Editable
import android.view.View
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.views.Type
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.dialog_password_confirm_finger.*
import kotlinx.android.synthetic.main.dialog_password_confirm_finger.description
import kotlinx.android.synthetic.main.dialog_password_confirm_finger.pass
import kotlinx.android.synthetic.main.dialog_password_confirm_finger.passError

class ConfirmRemoveWalletDialog : BaseDialogFragment<ConfirmRemoveWalletPresenter>(), ConfirmRemoveWalletContract.View {
    private var withFingerprint = false
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPasswordChanged()
        }
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_password_confirm_finger

    companion object {
        var callback: ConfirmRemoveWalletContract.Callback? = null

        fun newInstance() = ConfirmRemoveWalletDialog().apply {
        }

        fun getFragmentTag(): String = ConfirmRemoveWalletDialog::class.java.simpleName
    }

    override fun init(isFingerprintEnable: Boolean) {
        withFingerprint = isFingerprintEnable

        description.setText(if (withFingerprint) R.string.remove_wallet_password_1 else R.string.remove_wallet_password_2)

        touchIDView.visibility = if (withFingerprint) View.VISIBLE else View.GONE

        clearPasswordError()
    }

    override fun onStart() {
        super.onStart()

        if (!withFingerprint) {
            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        }
    }

    override fun addListeners() {
        btnOk.setOnClickListener {
            presenter?.onOkPressed(pass.text?.toString() ?: "")
        }

        close.setOnClickListener {
            presenter?.onCancel()
        }

        pass.addTextChangedListener(passWatcher)

        if (withFingerprint)
        {
            cancellationSignal = CancellationSignal()

            authCallback = FingerprintCallback(presenter, cancellationSignal)

            FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                    authCallback!!, null)
        }

    }

    override fun clearListeners() {
        btnOk.setOnClickListener(null)
        close.setOnClickListener(null)
        pass.removeTextChangedListener(passWatcher)

        authCallback = null
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    override fun showErrorFingerprint() {
        touchIDView.setType(Type.ERROR)

        showSnackBar(getString(R.string.owner_key_verification_fingerprint_error))
    }

    override fun showFailedFingerprint() {
        touchIDView.setType(Type.FAILED)
    }

    override fun showSuccessFingerprint() {
        touchIDView.setType(Type.SUCCESS)
    }

    override fun clearPasswordError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun showWrongPasswordError() {
        pass.isStateError = true
        passError.text = getString(R.string.pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showEmptyPasswordError() {
        pass.isStateError = true
        passError.text = getString(R.string.password_can_not_be_empty)
        passError.visibility = View.VISIBLE
    }

    override fun close(success: Boolean) {
        dismiss()
        callback?.onClose(success)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ConfirmRemoveWalletPresenter(this, ConfirmRemoveWalletRepository())
    }

    private class FingerprintCallback(val presenter: ConfirmRemoveWalletContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            presenter?.onErrorFingerprint()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            presenter?.onSuccessFingerprint()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            presenter?.onFailedFingerprint()
        }
    }
}