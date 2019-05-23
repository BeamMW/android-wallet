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

package com.mw.beam.beamwallet.screens.send

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import com.mw.beam.beamwallet.core.helpers.PermissionsHelper
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.views.PasteEditTextWatcher
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import com.mw.beam.beamwallet.screens.send.confirmation_dialog.SendConfirmationDialog
import kotlinx.android.synthetic.main.fragment_send.*


/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendFragment : BaseFragment<SendPresenter>(), SendContract.View {
    private lateinit var tokenWatcher: TextWatcher
    private lateinit var amountWatcher: TextWatcher
    private lateinit var feeWatcher: TextWatcher
    private lateinit var feeFocusListener: View.OnFocusChangeListener
    private var dialog: androidx.fragment.app.DialogFragment? = null

    private val dialogListener = object : SendConfirmationDialog.OnConfirmedDialogListener {
        override fun onConfirmed() {
            presenter?.onSend()
        }

        override fun onClosed() {
            presenter?.onDialogClosePressed()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_send
    override fun getToolbarTitle(): String? = getString(R.string.send_title)

    override fun getAmount(): Double = try {
        amount.text.toString().toDouble()
    } catch (e: Exception) {
        0.0
    }
    override fun getToken(): String = token.text.toString()
    override fun getComment(): String? = comment.text.toString()
    override fun getFee(): Long {
        return try {
            fee.text.toString().toLong()
        } catch (exception: NumberFormatException) {
            0
        }
    }

    override fun init(defaultFee: Int) {
        setHasOptionsMenu(true)
        token.requestFocus()
        fee.setText(defaultFee.toString())
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter?.onConfirm()
        }

        btnSendAll.setOnClickListener {
            presenter?.onSendAllPressed()
        }

        scanQR.setOnClickListener {
            presenter?.onScanQrPressed()
        }

        tokenWatcher = object : PasteEditTextWatcher {
            override fun onPaste() {
                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

                if (clipboardManager?.hasPrimaryClip() == true) {
                    val item = clipboardManager.primaryClip?.getItemAt(0)
                    presenter?.onTokenPasted(token = item?.text.toString(), oldToken = token.text?.toString())
                }
            }

            override fun afterTextChanged(rawToken: Editable?) {
                presenter?.onTokenChanged(rawToken.toString())
            }
        }

        token.addListener(tokenWatcher)

        amountWatcher = object : TextWatcher {
            override fun afterTextChanged(token: Editable?) {
                presenter?.onAmountChanged()
            }
        }
        amount.addTextChangedListener(amountWatcher)
        amount.filters = Array<InputFilter>(1) { AmountFilter() }

        feeWatcher = object : TextWatcher {
            override fun afterTextChanged(token: Editable?) {
                presenter?.onFeeChanged(token.toString())
            }
        }
        fee.addTextChangedListener(feeWatcher)
        feeFocusListener = View.OnFocusChangeListener { _, isFocused ->
            presenter?.onFeeFocusChanged(isFocused, fee.text.toString())
        }
        fee.onFocusChangeListener = feeFocusListener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onScannedQR(IntentIntegrator.parseActivityResult(resultCode, data).contents)
    }

    override fun isPermissionGranted(): Boolean {
        return PermissionsHelper.requestPermissions(this, PermissionsHelper.PERMISSIONS_CAMERA, PermissionsHelper.REQUEST_CODE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var isGranted = true

        for ((index, permission) in permissions.withIndex()) {
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                isGranted = false
                if (!shouldShowRequestPermissionRationale(permission)) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.NEVER_ASK_AGAIN)
                } else if (PermissionsHelper.PERMISSIONS_CAMERA == permission) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.DECLINED)
                }
            }
        }

        if (isGranted) {
            presenter?.onRequestPermissionsResult(PermissionStatus.GRANTED)
        }
    }

    override fun showStayActiveDialog() {
        showAlert(getString(R.string.common_stay_active_message), getString(R.string.common_ok), title = getString(R.string.common_stay_active_title))
    }

    override fun showPermissionRequiredAlert() {
        showAlert(message = getString(R.string.send_permission_required_message),
                btnConfirmText = getString(R.string.send_permission_required_settings),
                onConfirm = { showAppDetailsPage() },
                title = getString(R.string.send_permission_required_title),
                btnCancelText = getString(R.string.common_cancel))
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

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.common_activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.common_cancel), { presenter?.onCancelDialog() })
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        activity?.invalidateOptionsMenu()

        val availableVisibility = if (isEnable || params.visibility != View.VISIBLE) View.GONE else View.VISIBLE
        availableTitle.visibility = availableVisibility
        availableSum.visibility = availableVisibility
        btnSendAll.visibility = availableVisibility
    }

    override fun showNotBeamAddressError() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }

    override fun showCantPasteError() {
        showSnackBar(getString(R.string.send_error_paste), R.color.common_text_color)
    }

    override fun scanQR() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.captureActivity = ScanQrActivity::class.java
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun setFee(feeAmount: String) {
        fee.setText(feeAmount)
        fee.setSelection(fee.text?.length ?: 0)
    }

    override fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean {
        val feeAmount = try {
            fee.text.toString().toLong().convertToBeam()
        } catch (exception: NumberFormatException) {
            0.0
        }
        var hasErrors = false
        clearErrors()

        try {
            if (amount.text.toString().toDouble() + feeAmount > availableAmount.convertToBeam()) {
                configAmountError(configAmountErrorMessage((availableAmount.convertToBeam() - feeAmount).convertToBeamString(), isEnablePrivacyMode))
                hasErrors = true
            }
        } catch (exception: NumberFormatException) {
            configAmountError(configAmountErrorMessage(availableAmount.convertToBeamString(), isEnablePrivacyMode))
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

    private fun configAmountErrorMessage(amountString: String, isEnablePrivacyMode: Boolean): String {
        return if (isEnablePrivacyMode) {
            getString(R.string.send_insufficient_funds_error)
        } else {
            String.format(getString(R.string.send_amount_overflow_error), amountString)
        }
    }

    override fun showConfirmDialog(token: String, amount: Double, fee: Long) {
        dialog = SendConfirmationDialog.newInstance(token, amount, fee, dialogListener)
        dialog?.show(childFragmentManager, SendConfirmationDialog.getFragmentTag())
    }

    override fun dismissDialog() {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }

    override fun setAddress(address: String) {
        token.setText(address)
        token.setSelection(token.text?.length ?: 0)
    }

    override fun setAmount(amount: Double) {
        this.amount.setText(amount.convertToBeamString())
        this.amount.setSelection(this.amount.text?.length ?: 0)
    }

    override fun setComment(comment: String) {
        this.comment.setText(comment)
        this.comment.setSelection(this.comment.text?.length ?: 0)
    }

    override fun showCantSendToExpiredError() {
        showAlert(getString(R.string.send_error_expired_address), getString(R.string.common_ok), {})
    }

    override fun setAddressError() {
        tokenError.visibility = View.VISIBLE
        tokenDescription.visibility = View.GONE
    }

    override fun clearAddressError() {
        tokenError.visibility = View.INVISIBLE
        tokenDescription.visibility = View.VISIBLE
    }

    override fun clearToken(clearedToken: String?) {
        token.setText(clearedToken)
        token.setSelection(token.text?.length ?: 0)
    }

    override fun clearErrors() {
        amountError.visibility = View.GONE
        amount.setTextColor(ContextCompat.getColor(context!!, R.color.sent_color))
        amount.isStateNormal = true
    }

    override fun updateUI(shouldShowParams: Boolean, defaultFee: Int, isEnablePrivacyMode: Boolean) {
        params.visibility = if (shouldShowParams) View.VISIBLE else View.GONE

        val availableVisibility = if (isEnablePrivacyMode || !shouldShowParams) View.GONE else View.VISIBLE
        availableTitle.visibility = availableVisibility
        availableSum.visibility = availableVisibility
        btnSendAll.visibility = availableVisibility

        if (shouldShowParams) {
            //clear previous input before showing to user
            amount.text = null
            comment.text = null
            fee.setText(defaultFee.toString())
        } else {
            //can't attach this view to the params because constraint group forbid to change visibility of it's children
            amountError.visibility = View.GONE
            tokenError.visibility = View.INVISIBLE
            tokenDescription.visibility = View.VISIBLE
        }
    }

    override fun updateAvailable(availableString: String) {
        availableSum.text = availableString
    }

    override fun isAmountErrorShown(): Boolean {
        return amountError.visibility == View.VISIBLE
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
        btnSendAll.setOnClickListener(null)
        token.removeListener(tokenWatcher)
        amount.removeTextChangedListener(amountWatcher)
        amount.filters = emptyArray()
        fee.onFocusChangeListener = null
        fee.removeTextChangedListener(feeWatcher)
    }

    private fun configAmountError(errorString: String) {
        amountError.visibility = View.VISIBLE
        amountError.text = errorString
        amount.setTextColor(ContextCompat.getColorStateList(context!!, R.color.text_color_selector))
        amount.isStateError = true
    }

    private fun showAppDetailsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context?.packageName}")
        startActivity(intent)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendPresenter(this, SendRepository(), SendState())
    }
}
