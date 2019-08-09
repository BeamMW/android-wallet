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

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.dialog_confirm_transaction.*
import kotlinx.android.synthetic.main.dialog_confirm_transaction.description
import kotlinx.android.synthetic.main.dialog_confirm_transaction.pass
import kotlinx.android.synthetic.main.dialog_confirm_transaction.passError
import kotlinx.android.synthetic.main.dialog_password_confirm.*
import kotlinx.android.synthetic.main.fragment_check_old_pass.*

class ConfirmTransactionDialog : BaseDialogFragment<ConfirmTransactionPresenter>(), ConfirmTransactionContract.View {
    private var withFingerprint = false
    private var delayedTask: DelayedTask? = null
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPasswordChanged()
        }
    }

    companion object {
        var callback: ConfirmTransactionContract.Callback? = null
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_confirm_transaction


    override fun init(isFingerprintEnable: Boolean) {
        withFingerprint = isFingerprintEnable

        description.setText(if (withFingerprint) R.string.use_fingerprint_ot_enter_your_password_to_confirm_transaction else R.string.enter_your_password_to_confirm_transaction)

        touchCard.visibility = if (withFingerprint) View.VISIBLE else View.GONE
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

        fingerprintError.visibility = View.INVISIBLE
        touchCard.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.fingerprint_card_background_color))
        fingerprintImage.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_touch))

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
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_error, ContextCompat.getColor(context!!, R.color.common_error_color_opacity_20))
        fingerprintError.visibility = View.VISIBLE

        showSnackBar(getString(R.string.owner_key_verification_fingerprint_error))
    }

    override fun showFailedFingerprint() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_error, ContextCompat.getColor(context!!, R.color.common_error_color_opacity_20))
        fingerprintError.visibility = View.VISIBLE

        delayedTask = DelayedTask.startNew(1, {
            fingerprintError.visibility = View.INVISIBLE
            animatedChangeDrawable(R.drawable.ic_touch, ContextCompat.getColor(context!!, R.color.fingerprint_card_background_color))
        })
    }

    override fun showSuccessFingerprint() {
        fingerprintError.visibility = View.INVISIBLE
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_success, ContextCompat.getColor(context!!, R.color.fingerprint_card_background_color))
    }

    private fun animatedChangeDrawable(resId: Int, cardColor: Int) {
        val fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        fadeOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                touchCard.setCardBackgroundColor(cardColor)
                touchCard.startAnimation(fadeIn)

                fingerprintImage.setImageDrawable(ContextCompat.getDrawable(context!!, resId))
                fingerprintImage.startAnimation(fadeIn)
            }
        })

        touchCard.startAnimation(fadeOut)
        fingerprintImage.startAnimation(fadeOut)
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
        findNavController().popBackStack()
        callback?.onClose(success)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ConfirmTransactionPresenter(this, ConfirmTransactionRepository())
    }

    private class FingerprintCallback(val presenter: ConfirmTransactionContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
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