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

package com.mw.beam.beamwallet.welcome_screen.welcome_passwords

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.views.PasswordStrengthView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.welcome_screen.OnBackPressedHandler
import kotlinx.android.synthetic.main.fragment_welcome_passwords.*


/**
 * Created by vain onnellinen on 10/23/18.
 */
class WelcomePasswordsFragment : BaseFragment<WelcomePasswordsPresenter>(), WelcomePasswordsContract.View, OnBackPressedHandler {
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

    override fun getSeed(): Array<String>? = arguments?.getStringArray(ARG_PHRASES)
    override fun getPass(): String = pass.text?.trim().toString()
    override fun proceedToWallet() = (activity as WelcomePasswordsHandler).proceedToWallet()
    override fun showSeedFragment() = (activity as WelcomePasswordsHandler).showSeedFragment()

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

    override fun showSeedAlert() {
        showAlert(message = getString(R.string.welcome_pass_return_seed_message),
                title = getString(R.string.welcome_pass_return_seed_title),
                btnConfirmText = getString(R.string.welcome_pass_return_seed_btn_create_new),
                btnCancelText = getString(R.string.common_cancel),
                onConfirm = { presenter.onCreateNewSeed() })
    }

    override fun clearListeners() {
        pass.removeTextChangedListener(passWatcher)
        confirmPass.removeTextChangedListener(confirmPassWatcher)
        btnProceed.setOnClickListener(null)
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
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
        fun showSeedFragment()
    }
}
