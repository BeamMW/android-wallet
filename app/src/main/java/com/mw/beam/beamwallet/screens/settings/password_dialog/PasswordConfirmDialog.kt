package com.mw.beam.beamwallet.screens.settings.password_dialog

import android.content.DialogInterface
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.dialog_password_confirm.*

class PasswordConfirmDialog: BaseDialogFragment<PasswordConfirmPresenter>(), PasswordConfirmContract.View {
    lateinit var presenter: PasswordConfirmPresenter
    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter.onPasswordChanged()
        }
    }

    companion object {
        fun newInstance(onConfirm: () -> Unit, onCancel: () -> Unit) = PasswordConfirmDialog().apply {
            this.onConfirm = onConfirm
            this.onCancel = onCancel
        }

        fun getFragmentTag(): String = PasswordConfirmDialog::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_password_confirm

    override fun confirm() {
        onConfirm?.invoke()
        dismiss()
    }

    override fun cancel() {
        dismiss()
    }

    override fun addListeners() {
        btnConfirm.setOnClickListener { presenter.onConfirm(pass.text.toString()) }
        btnCancel.setOnClickListener { presenter.onCancelDialog() }
        pass.addTextChangedListener(passWatcher)
    }

    override fun clearListeners() {
        btnConfirm.setOnClickListener(null)
        btnCancel.setOnClickListener(null)
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

    override fun onDismiss(dialog: DialogInterface?) {
        onCancel?.invoke()

        onCancel = null
        onConfirm = null
        super.onDismiss(dialog)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = PasswordConfirmPresenter(this, PasswordConfirmRepository())
        return presenter
    }
}