package com.mw.beam.beamwallet.screens.confirm


import android.os.Handler
import android.text.Editable
import android.view.View
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.content.ContextCompat

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.views.Type
import com.mw.beam.beamwallet.core.watchers.TextWatcher

import kotlinx.android.synthetic.main.dialog_password_confirm.pass
import kotlinx.android.synthetic.main.dialog_password_confirm.passError
import kotlinx.android.synthetic.main.dialog_password_confirm_finger.*

class PasswordConfirmDialog: BaseDialogFragment<PasswordConfirmPresenter>(), PasswordConfirmContract.View {

    enum class Mode {
        RemoveWallet, ChangeNode, ChangeSettings, SendBeam
    }

    private var withFingerprint = false
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPasswordChanged()
        }
    }

    private var onConfirm: (() -> Unit)? = null
    private var onDismiss: (() -> Unit)? = null

    private var mode = Mode.ChangeNode


    companion object {
        fun newInstance(mode: Mode, onConfirm: () -> Unit, onDismiss: () -> Unit) = PasswordConfirmDialog().apply {
            this.onConfirm = onConfirm
            this.onDismiss = onDismiss
            this.mode = mode
        }


        fun getFragmentTag(): String = PasswordConfirmDialog::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_password_confirm_finger

    override fun init(isFingerprintEnable: Boolean) {
        withFingerprint = isFingerprintEnable

        when (mode) {
            Mode.RemoveWallet -> description.setText(if (withFingerprint) R.string.remove_wallet_password_1 else R.string.remove_wallet_password_2)
            Mode.ChangeNode -> description.setText(if (withFingerprint) R.string.change_node_text_2 else R.string.change_node_text_3)
            Mode.ChangeSettings -> description.setText(if (withFingerprint) R.string.change_settings_text_1 else R.string.change_settings_text_2)
            Mode.SendBeam -> description.setText(if (withFingerprint) R.string.use_fingerprint_ot_enter_your_password_to_confirm_transaction else R.string.enter_your_password_to_confirm_transaction)
        }

        if (mode == Mode.SendBeam) btnOk.background = ContextCompat.getDrawable(context!!, R.drawable.send_button)

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

        when {
            success -> onConfirm?.invoke()
            else -> onDismiss?.invoke()
        }
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return PasswordConfirmPresenter(this, PasswordConfirmRepository())
    }

    private class FingerprintCallback(val presenter: PasswordConfirmPresenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
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