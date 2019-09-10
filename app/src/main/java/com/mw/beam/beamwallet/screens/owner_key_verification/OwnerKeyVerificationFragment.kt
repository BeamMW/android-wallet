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

package com.mw.beam.beamwallet.screens.owner_key_verification

import android.os.Handler
import android.text.Editable
import android.view.View
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_owner_key_verification.*
import kotlinx.android.synthetic.main.fragment_owner_key_verification.pass
import kotlinx.android.synthetic.main.fragment_owner_key_verification.passError
import com.mw.beam.beamwallet.core.views.Type

class OwnerKeyVerificationFragment: BaseFragment<OwnerKeyVerificationPresenter>(), OwnerKeyVerificationContract.View {
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null

    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter?.onChangePassword()
        }
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_owner_key_verification

    override fun getToolbarTitle(): String? = getString(R.string.show_owner_key)

    override fun init(isEnableFingerprint: Boolean) {
        enterPasswordTitle.setText(R.string.enter_your_current_password)
        verificationDescription.visibility = View.GONE

        if (isEnableFingerprint)
        {
            cancellationSignal = CancellationSignal()
            authCallback = FingerprintCallback(presenter, cancellationSignal)

            FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                    authCallback!!, null)
        }
    }

    override fun onStart() {
        super.onStart()

        displayFingerPrint((presenter?.isEnableFingerprint() == true))

        if ((presenter?.isEnableFingerprint() == false))
        {
            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        }
    }

    override fun displayFingerPrint(display: Boolean) {
        mainScrollView.visibility = if (display) View.GONE else View.VISIBLE
        fingerMainView.visibility = if (display) View.VISIBLE else View.GONE
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter?.onNext()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun clearListeners() {
        pass.removeTextChangedListener(passWatcher)
    }

    override fun getPassword(): String = pass.text?.toString() ?: ""

    override fun showEmptyPasswordError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.password_can_not_be_empty)
        pass.isStateError = true
    }

    override fun showWrongPasswordError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.current_password_is_incorrect)
        pass.isStateError = true
    }

    override fun clearPasswordError() {
        passError.visibility = View.GONE
        pass.isStateAccent = true
    }

    override fun navigateToOwnerKey() {
        findNavController().navigate(OwnerKeyVerificationFragmentDirections.actionOwnerKeyVerificationFragmentToOwnerKeyFragment())
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return OwnerKeyVerificationPresenter(this, OwnerKeyVerificationRepository())
    }


    override fun clearFingerprintCallback() {
        authCallback = null
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    override fun showFailed() {
        touchIDView.setType(Type.FAILED)
    }

    override fun success() {
        touchIDView.setType(Type.SUCCESS)

        DelayedTask.startNew(1, {
            presenter?.onFingerprintSuccess()

            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        })
    }

    override fun error() {
        touchIDView.setType(Type.ERROR)

        showToast(getString(R.string.owner_key_verification_fingerprint_error),1000)
    }


    private class FingerprintCallback(val presenter: OwnerKeyVerificationContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            presenter?.onError()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            presenter?.onSuccess()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            presenter?.onFailed()
        }
    }
}