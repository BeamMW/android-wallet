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

package com.mw.beam.beamwallet.screens.confirm

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
import kotlinx.android.synthetic.main.fragment_double_authorization.*
import kotlinx.android.synthetic.main.fragment_double_authorization.pass
import kotlinx.android.synthetic.main.fragment_double_authorization.passError
import com.mw.beam.beamwallet.core.views.Status
import com.mw.beam.beamwallet.core.views.Type

class DoubleAuthorizationFragment: BaseFragment<DoubleAuthorizationPresenter>(), DoubleAuthorizationContract.View {


    private fun type(): DoubleAuthorizationFragmentMode {
        return DoubleAuthorizationFragmentArgs.fromBundle(arguments!!).type
    }

    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null

    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter?.onChangePassword()
        }
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_double_authorization

    override fun getToolbarTitle(): String? {
        return when {
            type() == DoubleAuthorizationFragmentMode.OwnerKey -> getString(R.string.show_owner_key)
            type() == DoubleAuthorizationFragmentMode.DisplaySeed -> getString(R.string.show_seed_phrase)
            else -> getString(R.string.complete_verification)
        }
    }


    override fun init(isEnableFingerprint: Boolean) {
        enterPasswordTitle.setText(R.string.enter_your_current_password)
        verificationDescription.visibility = View.GONE

        if (isEnableFingerprint)
        {
            if(presenter?.isEnableFaceID() == true)
            {
                biometricView.setBiometricType(Type.FACE)
                textView.text = getString(R.string.use_faceid)
            }
            else if(presenter?.isEnableFingerprint() == true) {
                biometricView.setBiometricType(Type.FINGER)

                cancellationSignal = CancellationSignal()
                authCallback = FingerprintCallback(presenter, cancellationSignal)

                FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                        authCallback!!, null)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        displayFingerPrint((presenter?.isEnableFingerprint() == true) || presenter?.isEnableFaceID() == true)

        if(presenter?.isEnableFaceID() == true)
        {
            displayFaceIDPrompt()
        }
        else if ((presenter?.isEnableFingerprint() == false))
        {
            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        }
    }

    private fun displayFaceIDPrompt() {
        biometricView.visibility = View.GONE
        textView.visibility = View.GONE

        App.self.showFaceIdPrompt(this,getString(R.string.use_faceid_access_wallet).replace(".",""), getString(R.string.cancel)) {
            when (it) {
                Status.SUCCESS -> {
                    success()
                    presenter?.onSuccess()
                }
                Status.CANCEL -> {
                    findNavController().popBackStack()
                }
                Status.ERROR -> {
                    findNavController().popBackStack()
                }
                Status.FAILED -> {
                    findNavController().popBackStack()
                }
            }
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

    override fun navigateToNextScreen() {

        if(type() == DoubleAuthorizationFragmentMode.OwnerKey) {
            findNavController().navigate(DoubleAuthorizationFragmentDirections.actionOwnerKeyVerificationFragmentToOwnerKeyFragment())
        }
        else if (type() == DoubleAuthorizationFragmentMode.DisplaySeed){
            findNavController().navigate(DoubleAuthorizationFragmentDirections.actionDoubleAuthorizationFragmentToWelcomeSeedFragment2(true))
        }
        else if (type() == DoubleAuthorizationFragmentMode.VerificationSeed){
            findNavController().navigate(DoubleAuthorizationFragmentDirections.actionDoubleAuthorizationFragmentToWelcomeSeedFragment2(false))
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return DoubleAuthorizationPresenter(this, DoubleAuthorizationRepository())
    }


    override fun clearFingerprintCallback() {
        authCallback = null
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    override fun showFailed() {
        biometricView.setStatus(Status.FAILED)
    }

    override fun success() {
        biometricView.setStatus(Status.SUCCESS)

        DelayedTask.startNew(1, {
            presenter?.onFingerprintSuccess()

            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        })
    }

    override fun error() {
        if (biometricView!=null) {
            biometricView.setStatus(Status.ERROR)
            if(presenter?.repository?.isFaceIDEnabled() == true)
            {
                showToast(getString(R.string.owner_key_verification_faceid_error),1000)
            }
            else{
                showToast(getString(R.string.owner_key_verification_fingerprint_error),1000)
            }
        }
    }


    private class FingerprintCallback(val presenter: DoubleAuthorizationContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
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