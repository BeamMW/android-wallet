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

package com.mw.beam.beamwallet.screens.create_password

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
import com.mw.beam.beamwallet.screens.welcome_screen.OnBackPressedHandler
import kotlinx.android.synthetic.main.fragment_passwords.*


/**
 * Created by vain onnellinen on 10/23/18.
 */
class PasswordFragment : BaseFragment<PasswordPresenter>(), PasswordContract.View, OnBackPressedHandler {
    private lateinit var presenter: PasswordPresenter

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
        private const val ARG_MODE_PASS_CHANGE = "ARG_MODE_PASS_CHANGE"

        fun newInstance(phrases: Array<String>) = PasswordFragment().apply { arguments = Bundle().apply { putStringArray(ARG_PHRASES, phrases) } }
        fun newInstance() = PasswordFragment().apply { arguments = Bundle().apply { putBoolean(ARG_MODE_PASS_CHANGE, true) } }
        fun getFragmentTag(): String = PasswordFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_passwords
    override fun getToolbarTitle(): String? = getString(R.string.pass_title)

    override fun init(isModeChangePass: Boolean) {
        if (isModeChangePass) {
            description.text = getString(R.string.pass_screen_change_description)
            btnProceed.textResId = R.string.pass_save_new
            btnProceed.iconResId = R.drawable.ic_btn_save
        } else {
            description.text = getString(R.string.pass_screen_description)
            btnProceed.textResId = R.string.pass_proceed_to_wallet
            btnProceed.iconResId = R.drawable.ic_btn_proceed
        }
    }

    override fun addListeners() {
        pass.addTextChangedListener(passWatcher)
        confirmPass.addTextChangedListener(confirmPassWatcher)

        btnProceed.setOnClickListener {
            presenter.onProceed()
        }
    }

    override fun getSeed(): Array<String>? = arguments?.getStringArray(ARG_PHRASES)
    override fun getPass(): String = pass.text?.trim().toString()
    override fun isModeChangePass(): Boolean = arguments?.getBoolean(ARG_MODE_PASS_CHANGE, false) ?: false

    override fun proceedToWallet() = (activity as PasswordsHandler).proceedToWallet()
    override fun showSeedFragment() = (activity as PasswordsHandler).showSeedFragment()
    override fun completePassChanging() = (activity as PassChangedHandler).onPassChanged()

    override fun hasErrors(): Boolean {
        var hasErrors = false
        clearErrors()

        if (pass.text.isNullOrBlank()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.pass_empty_error)
            pass.isStateError = true
            hasErrors = true
        }

        if (!pass.text.isNullOrBlank() && pass.text.toString() != confirmPass.text.toString()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.pass_not_match)
            confirmPass.isStateError = true
            hasErrors = true
        }

        if (confirmPass.text.isNullOrBlank()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.pass_empty_error)
            confirmPass.isStateError = true
            hasErrors = true
        }

        return hasErrors
    }

    override fun showOldPassError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.pass_old_pass_error)
        pass.isStateError = true
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
        showAlert(message = getString(R.string.pass_return_seed_message),
                title = getString(R.string.pass_return_seed_title),
                btnConfirmText = getString(R.string.pass_return_seed_btn_create_new),
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
        presenter = PasswordPresenter(this, PasswordRepository(), PasswordState())
        return presenter
    }

    interface PasswordsHandler {
        fun proceedToWallet()
        fun showSeedFragment()
    }

    interface PassChangedHandler {
        fun onPassChanged()
    }
}
