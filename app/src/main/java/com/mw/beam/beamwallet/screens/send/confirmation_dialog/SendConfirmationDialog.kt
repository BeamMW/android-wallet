package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.dialog_confirm_send.*

class SendConfirmationDialog : BaseDialogFragment<SendConfirmationPresenter>(), SendConfirmationContract.View {
    private lateinit var presenter: SendConfirmationPresenter
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter.onPasswordChanged()
        }
    }

    private var onConfirmedDialogListener: OnConfirmedDialogListener? = null

    companion object {
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_AMOUNT = "KEY_AMOUNT"
        private const val KEY_FEE = "KEY_FEE"

        fun newInstance(token: String?, amount: Double?, fee: Long?, dialogListener: OnConfirmedDialogListener): SendConfirmationDialog {
            return SendConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_TOKEN, token)
                    putDouble(KEY_AMOUNT, amount ?: 0.0)
                    putLong(KEY_FEE, fee ?: 0)
                }

                onConfirmedDialogListener = dialogListener
            }
        }

        fun getFragmentTag(): String = SendConfirmationDialog::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_confirm_send

    override fun init() {
        recipientValue.text = arguments?.getString(KEY_TOKEN)
        amountValue.text = getString(R.string.send_amount_beam, arguments?.getDouble(KEY_AMOUNT).toString())
        transactionFeeValue.text = getString(R.string.send_fee_groth, arguments?.getLong(KEY_FEE).toString())

        pass.addTextChangedListener(passWatcher)
        pass.requestFocus()
    }

    override fun addListeners() {
        btnConfirmSend.setOnClickListener { presenter.onSend(pass.text.toString()) }
        close.setOnClickListener { presenter.onCloseDialog() }
    }

    override fun clearListeners() {
        btnConfirmSend.setOnClickListener(null)
        close.setOnClickListener(null)
    }

    override fun clearPasswordError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun showPasswordError() {
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
        dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        onConfirmedDialogListener?.onDismissed()
        onConfirmedDialogListener = null
        super.onDismiss(dialog)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SendConfirmationPresenter(this, SendConfirmationRepository())
        return presenter
    }

    interface OnConfirmedDialogListener {
        fun onConfirmed()
        fun onDismissed()
    }
}