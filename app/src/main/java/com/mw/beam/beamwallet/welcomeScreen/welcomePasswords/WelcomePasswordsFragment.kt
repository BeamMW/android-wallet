package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.views.PasswordStrengthView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_passwords.*


/**
 * Created by vain onnellinen on 10/23/18.
 */
class WelcomePasswordsFragment : BaseFragment<WelcomePasswordsPresenter>(), WelcomePasswordsContract.View {
    private lateinit var presenter: WelcomePasswordsPresenter
    private var lettersSpace: Float = 0f
    private var dotsSpace: Float = 0f
    private lateinit var emptyPass: String
    private lateinit var veryWeakPass: String
    private lateinit var weakPass: String
    private lateinit var mediumPass: String
    private lateinit var mediumStrongPass: String
    private lateinit var strongPass: String
    private lateinit var veryStrongPass: String

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
        strengthText.text = when (strength) {
            PasswordStrengthView.Strength.EMPTY -> emptyPass
            PasswordStrengthView.Strength.VERY_WEAK -> veryWeakPass
            PasswordStrengthView.Strength.WEAK -> weakPass
            PasswordStrengthView.Strength.MEDIUM -> mediumPass
            PasswordStrengthView.Strength.MEDIUM_STRONG -> mediumStrongPass
            PasswordStrengthView.Strength.STRONG -> strongPass
            PasswordStrengthView.Strength.VERY_STRONG -> veryStrongPass
        }
    }

    override fun init() {
        val outValue = TypedValue()
        resources.getValue(R.dimen.welcome_password_letters_space, outValue, true)
        lettersSpace = outValue.float
        resources.getValue(R.dimen.welcome_password_dots_space, outValue, true)
        dotsSpace = outValue.float

        emptyPass = getString(R.string.welcome_pass_empty)
        veryWeakPass = getString(R.string.welcome_pass_very_weak)
        weakPass = getString(R.string.welcome_pass_weak)
        mediumPass = getString(R.string.welcome_pass_medium)
        mediumStrongPass = getString(R.string.welcome_pass_medium_strong)
        strongPass = getString(R.string.welcome_pass_strong)
        veryStrongPass = getString(R.string.welcome_pass_very_strong)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomePasswordsPresenter(this, WelcomePasswordsRepository())
        return presenter
    }

    interface WelcomePasswordsHandler {
        fun proceedToWallet()
    }
}
