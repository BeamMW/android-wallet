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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_open

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_open.*

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeOpenFragment : BaseFragment<WelcomeOpenPresenter>(), WelcomeOpenContract.View {
    private lateinit var presenter: WelcomeOpenPresenter
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter.onPassChanged()
        }
    }

    companion object {
        fun newInstance() = WelcomeOpenFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeOpenFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_open
    override fun getToolbarTitle(): String? = ""

    override fun addListeners() {
        btnOpen.setOnClickListener {
            presenter.onOpenWallet()
        }

        btnChange.setOnClickListener {
            presenter.onChangeWallet()
        }

        forgotPass.setOnClickListener {
            presenter.onForgotPassword()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun hasValidPass(): Boolean {
        var hasErrors = false
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true

        if (pass.text.isNullOrBlank()) {
            passError.text = getString(R.string.welcome_pass_empty_error)
            passError.visibility = View.VISIBLE
            pass.isStateError = true
            hasErrors = true
        }

        return !hasErrors
    }

    override fun clearError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun clearListeners() {
        btnOpen.setOnClickListener(null)
        btnChange.setOnClickListener(null)
        forgotPass.setOnClickListener(null)

        pass.removeTextChangedListener(passWatcher)
    }

    override fun getPass(): String = pass.text?.trim().toString()
    override fun openWallet() = (activity as OpenHandler).openWallet()
    override fun changeWallet() = (activity as OpenHandler).changeWallet()
    override fun restoreWallet() = (activity as OpenHandler).restoreWallet()

    override fun showOpenWalletError() {
        pass.isStateError = true
        passError.text = getString(R.string.welcome_pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showChangeAlert() {
        showAlert(getString(R.string.welcome_change_alert),
                getString(R.string.welcome_btn_change_alert),
                { presenter.onChangeConfirm() },
                getString(R.string.welcome_title_change_alert),
                getString(R.string.common_cancel))
    }

    override fun showForgotAlert() {
        showAlert(getString(R.string.welcome_forgot_alert),
                getString(R.string.welcome_btn_forgot_alert),
                { presenter.onForgotConfirm() },
                getString(R.string.welcome_title_forgot_alert),
                getString(R.string.common_cancel))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeOpenPresenter(this, WelcomeOpenRepository())
        return presenter
    }

    interface OpenHandler {
        fun openWallet()
        fun restoreWallet()
        fun changeWallet()
    }
}
