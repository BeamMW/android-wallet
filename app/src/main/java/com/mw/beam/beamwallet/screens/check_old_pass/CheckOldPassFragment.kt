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

package com.mw.beam.beamwallet.screens.check_old_pass

import android.text.Editable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_check_old_pass.*

/**
 * Created by vain onnellinen on 3/14/19.
 */
class CheckOldPassFragment : BaseFragment<CheckOldPassPresenter>(), CheckOldPassContract.View {
    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(password: Editable?) {
            presenter?.onPassChanged(password?.toString())
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_check_old_pass
    override fun getToolbarTitle(): String? = getString(R.string.change_password)

    override fun init() {
        passLayout.typeface = ResourcesCompat.getFont(context!!, R.font.roboto_regular)
    }

    override fun onStart() {
        super.onStart()

        pass.requestFocus()
        showKeyboard()
    }

    override fun addListeners() {
        pass.addTextChangedListener(passWatcher)

        btnNext.setOnClickListener {
            presenter?.onNext()
        }
    }

    override fun getPass(): String = pass.text.toString()

    override fun hasErrors(): Boolean {
        var hasErrors = false
        clearErrors()

        if (pass.text.isNullOrBlank()) {
            passError.visibility = View.VISIBLE
            passError.text = getString(R.string.password_can_not_be_empty)
            pass.isStateError = true
            hasErrors = true
        }

        return hasErrors
    }

    override fun showWrongPassError() {
        passError.visibility = View.VISIBLE
        passError.text = getString(R.string.current_password_is_incorrect)
        pass.isStateError = true
    }

    override fun showNewPassFragment() {
        findNavController().navigate(CheckOldPassFragmentDirections.actionCheckOldPassFragmentToPasswordChangeFragment(null, WelcomeMode.CHANGE_PASS.name, true))
    }


    override fun clearErrors() {
        passError.visibility = View.GONE
        pass.isStateAccent = true
    }

    override fun clearListeners() {
        pass.removeTextChangedListener(passWatcher)
        btnNext.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return CheckOldPassPresenter(this, CheckOldPassRepository())
    }
}
