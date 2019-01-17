package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.views.PasswordStrengthView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_passwords.*


/**
 * Created by vain onnellinen on 10/23/18.
 */
class WelcomePasswordsFragment : BaseFragment<WelcomePasswordsPresenter>(), WelcomePasswordsContract.View {
    private lateinit var presenter: WelcomePasswordsPresenter

    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter.onPassChanged(password?.toString())
        }
    }

    private val confirmPassWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter.onConfirmPassChanged()
        }
    }

    companion object {
        private const val ARG_PHRASES = "ARG_PHRASES"

        fun newInstance(phrases: Array<String>) = WelcomePasswordsFragment().apply { arguments = Bundle().apply { putStringArray(ARG_PHRASES, phrases) } }
        fun getFragmentTag(): String = WelcomePasswordsFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_passwords
    override fun getToolbarTitle(): String? = getString(R.string.welcome_passwords_title)

    override fun addListeners() {
        pass.addTextChangedListener(passWatcher)
        confirmPass.addTextChangedListener(confirmPassWatcher)

        btnProceed.setOnClickListener {
            presenter.onProceed()
        }
    }

    override fun getPhrases(): Array<String>? = arguments?.getStringArray(ARG_PHRASES)
    override fun getPass(): String = pass.text?.trim().toString()
    override fun proceedToWallet() = (activity as WelcomePasswordsHandler).proceedToWallet()

    override fun hasErrors(): Boolean {
        var hasErrors = false
        clearErrors()

        if (pass.text.isNullOrBlank()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.welcome_pass_empty_error)
            pass.isStateError = true
            hasErrors = true
        }

        if (!pass.text.isNullOrBlank() && pass.text.toString() != confirmPass.text.toString()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.welcome_passwords_not_match)
            confirmPass.isStateError = true
            hasErrors = true
        }

        if (confirmPass.text.isNullOrBlank()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.welcome_pass_empty_error)
            confirmPass.isStateError = true
            hasErrors = true
        }

        return hasErrors
    }

    override fun clearErrors() {
        passError.visibility = View.GONE

        if (pass.isFocused) {
            pass.isStateAccent = true
        } else {
            pass.isStateNormal = true
        }

        if (confirmPass.isFocused) {
            confirmPass.isStateAccent = true
        } else {
            confirmPass.isStateNormal = true
        }
    }

    override fun clearListeners() {
        pass.removeTextChangedListener(passWatcher)
        confirmPass.removeTextChangedListener(confirmPassWatcher)
        btnProceed.setOnClickListener(null)
    }

    override fun setStrengthLevel(strength: PasswordStrengthView.Strength) {
        strengthView.strength = strength
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomePasswordsPresenter(this, WelcomePasswordsRepository())
        return presenter
    }

    interface WelcomePasswordsHandler {
        fun proceedToWallet()
    }
}
