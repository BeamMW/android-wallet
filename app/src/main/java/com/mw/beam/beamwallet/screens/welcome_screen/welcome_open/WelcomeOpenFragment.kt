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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_open

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.Type
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_open.*
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback

/**
 *  10/19/18.
 */
class WelcomeOpenFragment : BaseFragment<WelcomeOpenPresenter>(), WelcomeOpenContract.View {
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPassChanged()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_open
    override fun getToolbarTitle(): String? = ""

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (App.isShowedLockScreen) {
            requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
        }
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun init(shouldInitFingerprint: Boolean) {
        if (!App.isShowedLockScreen) {
            App.isAuthenticated = false
        }
        else{
            btnChange.visibility = View.GONE
        }

        if (shouldInitFingerprint) {
            cancellationSignal = CancellationSignal()

            authCallback = FingerprintCallback(this, presenter, cancellationSignal)

            touchIDView.visibility = View.VISIBLE

            FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                    authCallback!!, null)

            description.setText(R.string.welcome_open_description_with_fingerprint)
        }
        else {
            description.setText(R.string.enter_your_password_to_access_the_wallet)

            touchIDView.visibility = View.GONE

            pass.requestFocus()

            Handler().postDelayed({
                pass.requestFocus()
                showKeyboard()
            }, 100)
        }

        passLayout.typeface = ResourcesCompat.getFont(context!!, R.font.roboto_regular)
    }

    override fun addListeners() {
        btnOpen.setOnClickListener {
            presenter?.onOpenWallet()
        }

        btnChange.setOnClickListener {
            presenter?.onChangeWallet()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun hasValidPass(): Boolean {
        var hasErrors = false
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true

        if (pass.text.isNullOrBlank()) {
            passError.text = getString(R.string.password_can_not_be_empty)
            passError.visibility = View.VISIBLE
            pass.isStateError = true
            hasErrors = true
        }

        return !hasErrors
    }

    fun showFailed() {
        touchIDView.setType(Type.FAILED)
    }

    fun fingerprintError() {
        touchIDView.setType(Type.ERROR)

        pass.requestFocus()

        showKeyboard()
    }

    fun success() {
        touchIDView.setType(Type.SUCCESS)
    }


    override fun clearError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun clearListeners() {
        btnOpen.setOnClickListener(null)
        btnChange.setOnClickListener(null)

        pass.removeTextChangedListener(passWatcher)
    }

    override fun clearFingerprintCallback() {
        authCallback?.clear()
        authCallback = null
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    override fun getPass(): String = pass.text?.toString() ?: ""

    override fun openWallet(pass: String) {
        if (App.isShowedLockScreen) {
            App.isShowedLockScreen = false
            findNavController().popBackStack()
        }
        else{
            findNavController().navigate(WelcomeOpenFragmentDirections.actionWelcomeOpenFragmentToWelcomeProgressFragment(pass, WelcomeMode.OPEN.name, null))
        }
    }
    override fun changeWallet() {
        findNavController().navigate(WelcomeOpenFragmentDirections.actionWelcomeOpenFragmentToWelcomeCreateFragment())
    }

    override fun back() {
        findNavController().navigate(WelcomeOpenFragmentDirections.actionWelcomeOpenFragmentToWelcomeCreateFragment())
    }

    override fun showOpenWalletError() {
        pass.isStateError = true
        passError.text = getString(R.string.pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showChangeAlert() {
        showAlert(getString(R.string.welcome_change_alert),
                getString(R.string.welcome_btn_change_alert),
                { presenter?.onChangeConfirm() },
                getString(R.string.welcome_title_change_alert),
                getString(R.string.cancel))
    }

    override fun showFingerprintAuthError() {
        showToast(getString(R.string.common_fingerprint_error), 3000)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WelcomeOpenPresenter(this, WelcomeOpenRepository())
    }

    private class FingerprintCallback(var view: WelcomeOpenFragment?, var presenter: WelcomeOpenContract.Presenter?, var cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            view?.fingerprintError()
            presenter?.onFingerprintError()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            view?.success()
            presenter?.onFingerprintSucceeded()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            view?.showFailed()
            presenter?.onFingerprintFailed()
        }

        fun clear() {
            view = null
            presenter = null
            cancellationSignal = null
        }
    }
}
