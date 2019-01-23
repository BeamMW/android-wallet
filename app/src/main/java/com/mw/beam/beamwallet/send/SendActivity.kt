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

package com.mw.beam.beamwallet.send

import android.support.v4.content.ContextCompat
import android.text.Editable
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.activity_send.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendActivity : BaseActivity<SendPresenter>(), SendContract.View {
    private lateinit var presenter: SendPresenter
    private lateinit var tokenWatcher: TextWatcher
    private lateinit var amountWatcher: TextWatcher
    private lateinit var feeFocusListener: View.OnFocusChangeListener

    override fun onControllerGetContentLayoutId() = R.layout.activity_send
    override fun getToolbarTitle(): String? = getString(R.string.send_title)

    override fun getAmount(): Double = amount.text.toString().toDouble()
    override fun getToken(): String = token.text.toString()
    override fun getComment(): String? = comment.text.toString()
    override fun getFee(): Long {
        return try {
            fee.text.toString().toLong()
        } catch (exception: NumberFormatException) {
            0
        }
    }

    override fun init() {
        token.requestFocus()
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter.onSend()
        }

        tokenWatcher = object : TextWatcher {
            override fun afterTextChanged(token: Editable?) {
                presenter.onTokenChanged(token.toString())
            }
        }
        token.addTextChangedListener(tokenWatcher)

        amountWatcher = object : TextWatcher {
            override fun afterTextChanged(token: Editable?) {
                presenter.onAmountChanged()
            }
        }
        amount.addTextChangedListener(amountWatcher)

        feeFocusListener = View.OnFocusChangeListener { _, isFocused ->
            if (!isFocused) {
                if (fee.text.toString().isEmpty()) {
                    fee.setText(getString(R.string.send_zero_fee))
                }
            }
        }
        fee.onFocusChangeListener = feeFocusListener
    }

    override fun hasErrors(availableAmount: Long): Boolean {
        val feeAmount = try {
            fee.text.toString().toLong().convertToBeam()
        } catch (exception: NumberFormatException) {
            0.0
        }
        var hasErrors = false
        clearErrors()

        try {
            if (amount.text.toString().toDouble() + feeAmount > availableAmount.convertToBeam()) {
                configAmountError(String.format(getString(R.string.send_amount_overflow_error), availableAmount.convertToBeamString()))
                hasErrors = true
            }
        } catch (exception: NumberFormatException) {
            configAmountError(String.format(getString(R.string.send_amount_overflow_error), availableAmount.convertToBeamString()))
            hasErrors = true
        }

        if (amount.text.isNullOrBlank()) {
            configAmountError(getString(R.string.send_amount_empty_error))
            hasErrors = true
        }

        try {
            if (amount.text.toString().toDouble() == 0.0) {
                configAmountError(getString(R.string.send_amount_zero_error))
                hasErrors = true
            }
        } catch (exception: NumberFormatException) {
            configAmountError(getString(R.string.send_amount_empty_error))
            hasErrors = true
        }

        return hasErrors
    }

    override fun setAddressError() {
        tokenError.visibility = View.VISIBLE
    }

    override fun clearAddressError() {
        tokenError.visibility = View.GONE
    }

    override fun clearToken(clearedToken: String?) {
        token.setText(clearedToken)
        token.setSelection(token.text.length)
    }

    override fun clearErrors() {
        amountError.visibility = View.GONE
        amount.setTextColor(ContextCompat.getColor(this, R.color.sent_color))
        amount.isStateNormal = true
    }

    override fun updateUI(shouldShowParams: Boolean) {
        params.visibility = if (shouldShowParams) View.VISIBLE else View.GONE

        if (shouldShowParams) {
            //clear previous input before showing to user
            amount.text = null
            comment.text = null
        } else {
            //can't attach this view to the params because constraint group forbid to change visibility of it's children
            amountError.visibility = View.GONE
            tokenError.visibility = View.GONE
        }
    }

    override fun close() {
        finish()
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
        token.removeTextChangedListener(tokenWatcher)
        amount.removeTextChangedListener(amountWatcher)
        fee.onFocusChangeListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SendPresenter(this, SendRepository(), SendState())
        return presenter
    }


    private fun configAmountError(errorString: String) {
        amountError.visibility = View.VISIBLE
        amountError.text = errorString
        amount.setTextColor(ContextCompat.getColorStateList(this, R.color.text_color_selector))
        amount.isStateError = true
    }
}
