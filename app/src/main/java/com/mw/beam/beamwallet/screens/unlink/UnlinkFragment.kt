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

package com.mw.beam.beamwallet.screens.unlink

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri

import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.util.Log

import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.InputFilterMinMax
import com.mw.beam.beamwallet.core.watchers.TextWatcher

import kotlinx.android.synthetic.main.fragment_unlink.*
import com.mw.beam.beamwallet.core.views.*
import org.jetbrains.anko.withAlpha

/**
 *  11/13/18.
 */
class UnlinkFragment : BaseFragment<UnlinkPresenter>(), UnlinkContract.View {
    private var minFee = 0
    private var maxFee = 0

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            presenter?.onAmountChanged()
        }
    }

    private val onFeeChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val p = progress + minFee
            presenter?.onFeeChanged(p.toString())
            updateFeeValue(p)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }


    override fun onControllerGetContentLayoutId() = R.layout.fragment_unlink
    override fun getToolbarTitle(): String? = getString(R.string.unlink)

    override fun getAmount(): Double = try {
        amount.text.toString().toDouble()
    } catch (e: Exception) {
        0.0
    }
    override fun getFee(): Long {
        val progress = feeSeekBar.progress.toLong() + minFee.toLong()
        return if (progress < 0) 0 else progress
    }

    @SuppressLint("SetTextI18n")
    override fun init(defaultFee: Int, max: Int) {
        maxFee = max

        setHasOptionsMenu(true)

        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
        maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"

        feeSeekBar.progress = 0
        updateFeeValue(defaultFee)

        ViewCompat.requestApplyInsets(contentScrollView)
        contentScrollView.smoothScrollTo(0, 0)
    }

    @SuppressLint("SetTextI18n")
    override fun setupMinFee(fee: Int) {
        minFee = fee

        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
    }

    override fun showMinFeeError() {
        showAlert(
                message = "",
                btnConfirmText = "",
                onConfirm = {}
        )
    }

    override fun requestFocusToAmount() {
        amount.requestFocus()
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.unlink_color)
    }

    override fun addListeners() {
        btnFeeKeyboard.setOnClickListener {
            presenter?.onLongPressFee()
        }

        btnNext.setOnClickListener {
            presenter?.onNext()
        }

        btnSendAll.setOnClickListener {
            presenter?.onSendAllPressed()
        }

        amount.addTextChangedListener(amountWatcher)
        amount.filters = Array<InputFilter>(1) { AmountFilter() }

        amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                amount.hint = "0"
                showKeyboard()
            } else {
                amount.hint = ""
                presenter?.onAmountUnfocused()
            }
        }

        feeSeekBar.setOnSeekBarChangeListener(onFeeChangeListener)

        if(App.isDarkMode) {
            amountContainer.setBackgroundColor(context!!.getColor(R.color.colorPrimary_dark).withAlpha(95))
        }
        else{
            amountContainer.setBackgroundColor(context!!.getColor(R.color.colorPrimary).withAlpha(95))
        }
    }


    override fun onHideKeyboard() {
        super.onHideKeyboard()
        if (amount.isFocused) {
            presenter?.onAmountUnfocused()
        }
    }


    @SuppressLint("InflateParams", "StringFormatInvalid")
    override fun showFeeDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_send_fee, null)

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dialog?.dismiss()
        }

        val secondAvailableSum = view.findViewById<TextView>(R.id.secondAvailableSum)
        secondAvailableSum.text = getFee().convertToCurrencyGrothString()

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.setText(R.string.unlinking_fee)

        val feeEditText = view.findViewById<AppCompatEditText>(R.id.feeEditText)
        feeEditText.setText(getFee().toString())
        feeEditText.filters = arrayOf(InputFilterMinMax(0, Int.MAX_VALUE))
        feeEditText.setTextColor(resources.getColor(R.color.colorAccent, null))

        feeEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                secondAvailableSum.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.feeError)?.visibility = View.GONE

                val rawFee = feeEditText.text?.toString()
                val fee = rawFee?.toLongOrNull() ?: 0
                secondAvailableSum.text = fee.convertToCurrencyGrothString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val btnSave = view.findViewById<BeamButton>(R.id.btnSave)
        btnSave.background = ContextCompat.getDrawable(App.self, R.drawable.common_button)
        btnSave.setOnClickListener {
            val rawFee = feeEditText.text?.toString()
            val fee = rawFee?.toLongOrNull() ?: 0
            if (fee >= minFee) {

                if(fee > presenter!!.MAX_FEE) {
                    maxFee = fee.toInt()

                    presenter!!.MAX_FEE = maxFee

                    maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"

                    feeSeekBar.max = maxFee - minFee
                }

                presenter?.onEnterFee(rawFee)

                dialog?.dismiss()
            }
            else {
                val feeErrorTextView = view.findViewById<TextView>(R.id.feeError)
                feeErrorTextView?.text = getString(R.string.min_fee_error, minFee.toString())
                feeErrorTextView.visibility = View.VISIBLE
                secondAvailableSum.visibility = View.GONE
            }
        }

        dialog = AlertDialog.Builder(context!!).setView(view).show().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        feeEditText.requestFocus()

        Handler().postDelayed({
            showKeyboard()
        }, 100)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onCreateOptionsMenu(menu, inflater)
    }

    override fun createOptionsMenu(menu: Menu?, inflater: MenuInflater, isEnablePrivacyMode: Boolean) {
        inflater.inflate(R.menu.privacy_menu, menu)
        val menuItem = menu?.findItem(R.id.privacy_mode)
        menuItem?.setOnMenuItemClickListener {
            presenter?.onChangePrivacyModePressed()
            false
        }

        menuItem?.setIcon(if (isEnablePrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showConfirmTransaction(amount: Long, fee: Long) {
        findNavController().navigate(UnlinkFragmentDirections.actionUnlinkFragmentToUnlinkConfirmationFragment(amount, fee))
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.cancel), { presenter?.onCancelDialog() })
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        activity?.invalidateOptionsMenu()

        val availableVisibility = if (isEnable) View.GONE else View.VISIBLE
        availableTitle.visibility = availableVisibility
        availableSum.visibility = availableVisibility
        secondAvailableSum.visibility = availableVisibility
        btnSendAll.visibility = availableVisibility
    }

    override fun setFee(feeAmount: String) {
        val fee = feeAmount.toIntOrNull() ?: 0
        feeSeekBar.progress = fee - minFee
        updateFeeValue(fee)
    }

    @SuppressLint("SetTextI18n")
    private fun updateFeeValue(progress: Int, clearAmountFocus: Boolean = true) {
        if (clearAmountFocus) {
            amount.clearFocus()
        }

        val params = feeProgressValue.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = (progress.toFloat() - minFee.toFloat()) / feeSeekBar.max.toFloat()

        val fee = if (progress < 0) 0 else progress

        feeProgressValue.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"
        feeProgressValue.layoutParams = params

        val feeString = "(${if (fee > 0) "+" else ""}$fee ${getString(R.string.currency_groth).toUpperCase()} ${getString(R.string.transaction_fee).toLowerCase()})"
        usedFee.text = getAmount().convertToCurrencyString() + " " + feeString
    }

    override fun updateFeeTransactionVisibility() {
        usedFee.visibility = if ((getAmount() > 0.0) && amountError.visibility == View.GONE) View.VISIBLE else View.GONE
        if (usedFee.visibility == View.VISIBLE) {
            updateFeeValue(feeSeekBar.progress+minFee, false)
        }
    }

    override fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean {
        var hasErrors = false
        clearErrors()

        if (hasAmountError(getAmount().convertToGroth(), getFee(), availableAmount, isEnablePrivacyMode)) {
            hasErrors = true
        }

        return hasErrors
    }

    override fun hasAmountError(amount: Long, fee: Long, availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean {
        return try {
            when {
                this.amount.text.isNullOrBlank() -> {
                    configAmountError(getString(R.string.send_amount_empty_error))
                    true
                }
                amount == 0L && fee < availableAmount -> {
                    configAmountError(getString(R.string.send_amount_zero_error))
                    true
                }
                amount + fee > availableAmount -> {
                    configAmountError(configAmountErrorMessage(((availableAmount - (amount + fee)) * -1).convertToBeamString(), isEnablePrivacyMode))
                    true
                }
                else -> false
            }
        } catch (exception: NumberFormatException) {
            configAmountError(configAmountErrorMessage(amount.convertToBeamString(), isEnablePrivacyMode))
            true
        }
    }

    private fun configAmountErrorMessage(amountString: String, isEnablePrivacyMode: Boolean): String {
        return if (isEnablePrivacyMode) {
            getString(R.string.insufficient_funds)
        } else {
            getString(R.string.send_amount_overflow_error, amountString)
        }
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun setAmount(amount: Double) {
        this.amount.setText(amount.convertToBeamString())
        this.amount.setSelection(this.amount.text?.length ?: 0)
    }

    override fun clearErrors() {
        amountError.visibility = View.GONE
        amount.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
        amount.isStateNormal = true
        updateFeeTransactionVisibility()
    }

    override fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean) {
        configPrivacyStatus(isEnablePrivacyMode)

        amount.text = null

        feeSeekBar.progress = defaultFee - minFee

        updateFeeValue(defaultFee)
    }

    override fun updateFeeViews(clearAmountFocus: Boolean) {
        amount.setTextColor(ContextCompat.getColorStateList(context!!, R.color.colorAccent))
        updateFeeValue(feeSeekBar.progress+minFee, clearAmountFocus)
    }

    @SuppressLint("SetTextI18n")
    override fun updateAvailable(available: Long) {
        btnSendAll.isEnabled = available > 0
        availableSum.text = "${available.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondAvailableSum.text = available.convertToCurrencyString()
    }

    override fun isAmountErrorShown(): Boolean {
        return amountError.visibility == View.VISIBLE
    }

    override fun clearListeners() {
        btnFeeKeyboard.setOnClickListener(null)
        btnNext.setOnClickListener(null)
        btnSendAll.setOnClickListener(null)
        amount.removeTextChangedListener(amountWatcher)
        amount.filters = emptyArray()
        amount.onFocusChangeListener = null
        feeSeekBar.setOnSeekBarChangeListener(null)
        feeContainer.setOnLongClickListener(null)
    }

    private fun configAmountError(errorString: String) {
        amountError.visibility = View.VISIBLE
        amountError.text = errorString
        amount.setTextColor(ContextCompat.getColorStateList(context!!, R.color.text_color_selector))
        amount.isStateError = true
        updateFeeTransactionVisibility()
    }

    private fun showAppDetailsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context?.packageName}")
        startActivity(intent)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return UnlinkPresenter(this, UnlinkRepository(), UnlinkState())
    }
}
