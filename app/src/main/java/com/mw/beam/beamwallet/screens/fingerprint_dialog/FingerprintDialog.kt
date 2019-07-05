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

package com.mw.beam.beamwallet.screens.fingerprint_dialog

import android.content.DialogInterface
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.fragment.app.FragmentManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import kotlinx.android.synthetic.main.dialog_fingerprint.*

class FingerprintDialog: BaseDialogFragment<FingerprintDialogPresenter>(), FingerprintDialogContract.View {
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null

    var onSuccess: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null
    var onError: (() -> Unit)? = null

    private var isCancel = true

    private var delayedTask: DelayedTask? = null

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_fingerprint

    companion object {
        private const val TAG = "com.mw.beam.beamwallet.screens.fingerprint_dialog.FingerprintDialog"

        fun show(fragmentManager: FragmentManager, onSuccess: () -> Unit, onCancel: () -> Unit, onError: () -> Unit): FingerprintDialog {
            val dialog = FingerprintDialog()
            dialog.onSuccess = onSuccess
            dialog.onCancel = onCancel
            dialog.onError = onError

            dialog.show(fragmentManager, TAG)
            return dialog
        }

    }

    override fun init() {
        cancellationSignal = CancellationSignal()

        authCallback = FingerprintCallback(presenter, cancellationSignal)

        FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                authCallback!!, null)
    }


    override fun addListeners() {
        btnCancel.setOnClickListener {
            presenter?.onCancel()
        }
    }

    override fun clearListeners() {
        btnCancel.setOnClickListener(null)
    }

    override fun showFailed() {
        animatedChangeDrawable(R.drawable.ic_touch_error)
        delayedTask?.cancel(true)
        delayedTask = DelayedTask.startNew(1, { animatedChangeDrawable(R.drawable.ic_touch) })
    }

    override fun success() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_success)

        DelayedTask.startNew(1, {
            isCancel = false
            onSuccess?.invoke()
            dismiss()
        })
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

    override fun error() {
        delayedTask?.cancel(true)

        animatedChangeDrawable(R.drawable.ic_touch_error)
        isCancel = false
        onError?.invoke()
        dismiss()
    }

    override fun cancel() {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        delayedTask?.cancel(true)

        if (isCancel) {
            onCancel?.invoke()
        }
        super.onDismiss(dialog)
    }

    override fun clearFingerprintCallback() {
        authCallback = null
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return FingerprintDialogPresenter(this, FingerprintDialogRepository())
    }


    private class FingerprintCallback(val presenter: FingerprintDialogContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
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