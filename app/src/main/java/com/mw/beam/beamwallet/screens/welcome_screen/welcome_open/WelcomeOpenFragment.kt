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

import android.text.Editable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.visible
import com.mw.beam.beamwallet.core.views.visibleOrGone
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_open.*


/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeOpenFragment : BaseFragment<WelcomeOpenPresenter>(), WelcomeOpenContract.View {
    private var delayedTask: DelayedTask? = null
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPassChanged()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_open
    override fun getToolbarTitle(): String? = ""

    override fun init(shouldInitFingerprint: Boolean) {
        App.isAuthenticated = false

        if (shouldInitFingerprint) {
            cancellationSignal = CancellationSignal()

            authCallback = FingerprintCallback(this, presenter, cancellationSignal)

            fingerprintImage.setImageDrawable(context?.getDrawable(R.drawable.ic_touch))

            FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                    authCallback!!, null)

            description.setText(R.string.welcome_open_description_with_fingerprint)
        } else {
            description.setText(R.string.enter_your_password_to_access_the_wallet)
        }

        btnTouch.visibleOrGone(shouldInitFingerprint, false)

        passLayout.typeface = ResourcesCompat.getFont(context!!, R.font.roboto_regular)

        pass.setText("123",android.widget.TextView.BufferType.EDITABLE);
        presenter?.onOpenWallet()
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
        animatedChangeDrawable(R.drawable.ic_touch_error)
        delayedTask?.cancel(true)
        delayedTask = DelayedTask.startNew(1, { animatedChangeDrawable(R.drawable.ic_touch) })
    }

    fun fingerprintError() {
        delayedTask?.cancel(true)

        fingerprintImage.setImageDrawable(context?.getDrawable(R.drawable.ic_touch_error))
    }

    fun success() {
        delayedTask?.cancel(true)

        fingerprintImage.setImageDrawable(context?.getDrawable(R.drawable.ic_touch_success))
    }

    private fun animatedChangeDrawable(resId: Int) {
        val fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        fadeOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                fingerprintImage.setImageDrawable(context?.getDrawable(resId))
                fingerprintImage.startAnimation(fadeIn)
            }
        })
        fingerprintImage.startAnimation(fadeOut)
    }

    override fun clearError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun clearListeners() {
        delayedTask?.cancel(true)

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
        findNavController().navigate(WelcomeOpenFragmentDirections.actionWelcomeOpenFragmentToWelcomeProgressFragment(pass, WelcomeMode.OPEN.name, null))
    }
    override fun changeWallet() {
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
        showSnackBar(getString(R.string.common_fingerprint_error))
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
