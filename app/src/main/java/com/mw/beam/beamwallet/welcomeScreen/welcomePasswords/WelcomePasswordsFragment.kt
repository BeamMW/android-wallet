package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
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

    companion object {
        fun newInstance(): WelcomePasswordsFragment {
            val args = Bundle()
            val fragment = WelcomePasswordsFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomePasswordsFragment::class.java.simpleName
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_welcome_passwords, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = WelcomePasswordsPresenter(this, WelcomePasswordsRepository())
        configPresenter(presenter)

        pass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(password: Editable?) {
                presenter.onPassChanged(password?.toString())
            }
        })

        confirmPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(password: Editable?) {
                presenter.onConfirmPassChanged()
            }
        })

        btnProceed.setOnClickListener {
            presenter.onProceed()
        }

        showPass.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    presenter.onChangePassVisibility(true)
                }
                MotionEvent.ACTION_UP -> {
                    presenter.onChangePassVisibility(false)
                }
            }
            true
        }
    }

    override fun getPass(): String = pass.text.trim().toString()

    override fun hasErrors(): Boolean {
        val context = context ?: return false
        var hasErrors = false
        pass.error = null
        passMatchError.visibility = View.GONE
        confirmPass.setTextColor(ContextCompat.getColor(context, R.color.common_text_color))

        if (pass.text.isNullOrBlank()) {
            pass.error = getString(R.string.welcome_pass_empty_error)
            hasErrors = true
        }

        if (!pass.text.isNullOrBlank() && pass.text.toString() != confirmPass.text.toString()) {
            passMatchError.visibility = View.VISIBLE
            passMatchError.text = getString(R.string.welcome_passwords_not_match)
            confirmPass.setTextColor(ContextCompat.getColor(context, R.color.common_error_color))
            hasErrors = true
        }

        if (confirmPass.text.isNullOrBlank()) {
            passMatchError.text = getString(R.string.welcome_pass_empty_error)
            hasErrors = true
        }

        return hasErrors
    }

    override fun clearErrors() {
        val context = context ?: return
        pass.error = null
        confirmPass.setTextColor(ContextCompat.getColor(context, R.color.common_text_color))
        passMatchError.visibility = View.GONE
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

    override fun proceedToWallet() {
        (activity as WelcomePasswordsHandler).proceedToWallet()
    }

    override fun changePassVisibility(shouldShow: Boolean) {
        confirmPass.inputType = if (shouldShow) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPass.setSelection(confirmPass.text.length)
        confirmPass.letterSpacing = if (shouldShow) lettersSpace else dotsSpace
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

    interface WelcomePasswordsHandler {
        fun proceedToWallet()
    }
}
