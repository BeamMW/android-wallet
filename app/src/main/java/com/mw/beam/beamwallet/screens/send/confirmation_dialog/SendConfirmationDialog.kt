package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.dialog_confirm_send.*

class SendConfirmationDialog : BaseDialogFragment<SendConfirmationPresenter>(), SendConfirmationContract.View {
    private var cancellationSignal: CancellationSignal? = null
    private var authCallback: FingerprintManagerCompat.AuthenticationCallback? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPasswordChanged()
        }
    }

    private var onConfirmedDialogListener: OnConfirmedDialogListener? = null

    companion object {
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_AMOUNT = "KEY_AMOUNT"
        private const val KEY_FEE = "KEY_FEE"

        fun newInstance(token: String, amount: Double, fee: Long, dialogListener: OnConfirmedDialogListener): SendConfirmationDialog {
            return SendConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_TOKEN, token)
                    putDouble(KEY_AMOUNT, amount)
                    putLong(KEY_FEE, fee)
                }

                onConfirmedDialogListener = dialogListener
            }
        }

        fun getFragmentTag(): String = SendConfirmationDialog::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_confirm_send
    override fun getToken(): String? = arguments?.getString(KEY_TOKEN)
    override fun getAmount(): Double? = arguments?.getDouble(KEY_AMOUNT)
    override fun getFee(): Long? = arguments?.getLong(KEY_FEE)

    override fun init(token: String?, amount: Double?, fee: Long?, shouldInitFingerprint: Boolean) {
        recipientValue.text = token
        amountValue.text = getString(R.string.send_amount_beam, amount?.convertToBeamString())
        transactionFeeValue.text = getString(R.string.send_fee_groth, fee.toString())

        if (shouldInitFingerprint) {
            cancellationSignal = CancellationSignal()

            authCallback = FingerprintCallback(presenter, cancellationSignal)

            FingerprintManagerCompat.from(App.self).authenticate(FingerprintManager.cryptoObject, 0, cancellationSignal,
                    authCallback!!, null)

            dialogDescription.setText(R.string.send_dialog_description_with_fingerprint)
        } else {
            dialogDescription.setText(R.string.send_dialog_description)
        }

        pass.requestFocus()
    }

    override fun showFingerprintAuthError() {
        showSnackBar(getString(R.string.common_fingerprint_error))
    }

    override fun addListeners() {
        btnConfirmSend.setOnClickListener { presenter?.onSend(pass.text.toString()) }
        close.setOnClickListener { presenter?.onCloseDialog() }
        pass.addTextChangedListener(passWatcher)
    }

    override fun clearListeners() {
        btnConfirmSend.setOnClickListener(null)
        close.setOnClickListener(null)
        pass.removeTextChangedListener(passWatcher)
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
        passError.text = getString(R.string.pass_empty_error)
        passError.visibility = View.VISIBLE
    }

    override fun confirm() {
        onConfirmedDialogListener?.onConfirmed()
    }

    override fun close() {
        onConfirmedDialogListener?.onClosed()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onConfirmedDialogListener = null
        super.onDismiss(dialog)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendConfirmationPresenter(this, SendConfirmationRepository())
    }

    interface OnConfirmedDialogListener {
        fun onConfirmed()
        fun onClosed()
    }

    private class FingerprintCallback(val presenter: SendConfirmationContract.Presenter?, val cancellationSignal: CancellationSignal?): FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            presenter?.onFingerprintError()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            presenter?.onFingerprintSucceeded()
            cancellationSignal?.cancel()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            presenter?.onFingerprintFailed()
        }
    }
}
