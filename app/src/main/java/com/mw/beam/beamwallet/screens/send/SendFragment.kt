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

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.views.PasteEditTextWatcher
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.address_edit.CategoryAdapter
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.app_activity.PendingSendInfo
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_send.*


/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendFragment : BaseFragment<SendPresenter>(), SendContract.View {
    private val tokenWatcher: TextWatcher = object : PasteEditTextWatcher {
        override fun onPaste() {}

        override fun afterTextChanged(rawToken: Editable?) {
            presenter?.onTokenChanged(rawToken.toString())
        }
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            presenter?.onAmountChanged()
        }
    }

    private val changeAddressCallback = object : ChangeAddressCallback {
        override fun onChangeAddress(walletAddress: WalletAddress) {
            presenter?.onAddressChanged(walletAddress)
        }
    }

    private val feeWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            presenter?.onFeeChanged(token.toString())
        }
    }

    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter?.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })
        }
    }

    private val onFeeChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            presenter?.onFeeChanged(progress.toString())
            updateFeeValue(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_send
    override fun getToolbarTitle(): String? = getString(R.string.send)

    override fun getAddressFromArguments(): String? {
        return SendFragmentArgs.fromBundle(arguments!!).address
    }

    override fun getAmountFromArguments(): Long {
        return SendFragmentArgs.fromBundle(arguments!!).amount
    }

    override fun getAmount(): Double = try {
        amount.text.toString().toDouble()
    } catch (e: Exception) {
        0.0
    }

    override fun getToken(): String = token.text.toString()
    override fun getComment(): String? = comment.text.toString()
    override fun getFee(): Long {
        val progress = feeSeekBar.progress.toLong()
        return if (progress <= 0) 1 else progress
    }

    @SuppressLint("SetTextI18n")
    override fun init(defaultFee: Int, maxFee: Int) {
        commentTitle.text = "${getString(R.string.transaction_comment)} (${getString(R.string.wont_be_shared)})"

        setHasOptionsMenu(true)
        token.requestFocus()
        feeSeekBar.max = maxFee
        minFeeValue.text = "1 ${getString(R.string.currency_groth).toUpperCase()}"
        maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"

        feeSeekBar.progress = defaultFee
        updateFeeValue(defaultFee)

        ArrayAdapter.createFromResource(
                context!!,
                R.array.receive_expires_periods,
                R.layout.receive_expire_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            expiresOnSpinner.adapter = adapter
            expiresOnSpinner.setSelection(0)
        }
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

        token.addListener(tokenWatcher)

        amount.addTextChangedListener(amountWatcher)
        amount.filters = Array<InputFilter>(1) { AmountFilter() }

        feeContainer.setOnLongClickListener {
            true
        }

        feeSeekBar.setOnSeekBarChangeListener(onFeeChangeListener)

        expiresOnSpinner.onItemSelectedListener = expireListener

        val advancedClickListener = View.OnClickListener {
            presenter?.onAdvancedPressed()
        }
        advancedTitle.setOnClickListener(advancedClickListener)
        btnExpandAdvanced.setOnClickListener(advancedClickListener)

        val editAddressClickListener = View.OnClickListener {
            presenter?.onEditAddressPressed()
        }
        editAddressTitle.setOnClickListener(editAddressClickListener)
        btnExpandEditAddress.setOnClickListener(editAddressClickListener)

        btnChangeAddress.setOnClickListener {
            presenter?.onChangeAddressPressed()
        }
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

    override fun showPermissionRequiredAlert() {
        showAlert(message = getString(R.string.send_permission_required_message),
                btnConfirmText = getString(R.string.settings),
                onConfirm = { showAppDetailsPage() },
                title = getString(R.string.send_permission_required_title),
                btnCancelText = getString(R.string.cancel))
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
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.cancel), { presenter?.onCancelDialog() })
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        activity?.invalidateOptionsMenu()

        TransitionManager.beginDelayedTransition(settingsContentLayout)

        val availableVisibility = if (isEnable) View.GONE else View.VISIBLE
        availableTitle.visibility = availableVisibility
        availableSum.visibility = availableVisibility
        btnSendAll.visibility = availableVisibility
    }

    override fun showNotBeamAddressError() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }

    override fun showCantPasteError() {
        showSnackBar(getString(R.string.send_error_paste))
    }

    override fun scanQR() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.captureActivity = ScanQrActivity::class.java
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun setFee(feeAmount: String) {
        val fee = feeAmount.toInt()
        feeSeekBar.progress = fee
        updateFeeValue(fee)
    }


    @SuppressLint("SetTextI18n")
    private fun updateFeeValue(progress: Int) {
        val params = feeProgressValue.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = if (progress <= 0) 0f else progress.toFloat() / feeSeekBar.max

        feeProgressValue.text = "${if (progress <= 0) 1 else progress} ${getString(R.string.currency_groth).toUpperCase()}"
        feeProgressValue.layoutParams = params
    }

    override fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean {
        val feeAmount = getFee().convertToBeam()
        var hasErrors = false
        clearErrors()

        if (!QrHelper.isValidAddress(getToken())) {
            hasErrors = true
            setAddressError()
        }

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
            getString(R.string.insufficient_funds)
        } else {
            "${getString(R.string.send_amount_overflow_error)} $amountString ${getString(R.string.currency_beam).toUpperCase()}"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun configOutgoingAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        outgoingAddressTitle.text = "${getString(R.string.outgoing_address).toUpperCase()}${if (isGenerated) " (${getString(R.string.autogenerated).toLowerCase()})" else ""}"
        outgoingAddress.text = walletAddress.walletID

        addressName.setText(walletAddress.label)

        expiresOnSpinner.setSelection(if (walletAddress.duration == 0L) ExpirePeriod.NEVER.ordinal else ExpirePeriod.DAY.ordinal)
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        clearFocus()

        animateDropDownIcon(btnExpandAdvanced, expand)
        TransitionManager.beginDelayedTransition(settingsContentLayout)
        advancedGroup.visibility = if (expand) View.VISIBLE else View.GONE
    }

    override fun handleExpandEditAddress(expand: Boolean) {
        clearFocus()

        animateDropDownIcon(btnExpandEditAddress, expand)
        TransitionManager.beginDelayedTransition(settingsContentLayout)
        editAddressGroup.visibility = if (expand) View.VISIBLE else View.GONE

        emptyCategoryListMessage.visibility = if (categorySpinner.adapter?.isEmpty != false && expand) View.VISIBLE else View.GONE
    }

    private fun clearFocus() {
        settingsContentLayout?.requestFocus()
    }

    override fun configCategory(currentCategory: Category?, categories: List<Category>) {
        categorySpinner.isEnabled = categories.isNotEmpty()

        emptyCategoryListMessage.visibility = if (categories.isEmpty() && editAddressGroup.visibility == View.VISIBLE) View.VISIBLE else View.GONE

        if (categories.isNotEmpty()) {
            categorySpinner.adapter = CategoryAdapter(context!!, arrayListOf(CategoryHelper.noneCategory).apply { addAll(categories) })

            if (currentCategory == null) {
                categorySpinner.setSelection(0)
            } else {
                categorySpinner.setSelection(categories.indexOfFirst { currentCategory.id == it.id } + 1)
            }
        }


        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    presenter?.onSelectedCategory(categories[position - 1])
                } else {
                    presenter?.onSelectedCategory(null)
                }
            }
        }
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 360f else 180f
        val angleTo = if (shouldExpand) 180f else 360f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun getCommentOutgoingAddress(): String {
        return addressName.text?.toString() ?: ""
    }

    override fun showChangeAddressFragment() {
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToChangeAddressFragment(callback = changeAddressCallback))
    }

    override fun setAddress(address: String) {
        token.setText(address)
        token.setSelection(token.text?.length ?: 0)
    }

    override fun setAmount(amount: Double) {
        clearFocus()
        this.amount.setText(amount.convertToBeamString())
        this.amount.setSelection(this.amount.text?.length ?: 0)
    }

    override fun setComment(comment: String) {
        this.comment.setText(comment)
        this.comment.setSelection(this.comment.text?.length ?: 0)
    }

    override fun showCantSendToExpiredError() {
        showAlert(getString(R.string.send_error_expired_address), getString(R.string.ok), {})
    }

    override fun setAddressError() {
        tokenError.visibility = View.VISIBLE
    }

    override fun clearAddressError() {
        tokenError.visibility = View.INVISIBLE
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

    override fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean) {
        configPrivacyStatus(isEnablePrivacyMode)

        amount.text = null
        comment.text = null

        feeSeekBar.progress = defaultFee
        updateFeeValue(defaultFee)

    }

    override fun updateFeeViews() {
        amount.setTextColor(ContextCompat.getColorStateList(context!!, R.color.sent_color))
        updateFeeValue(feeSeekBar.progress)
    }

    override fun pendingSendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        (activity as? AppActivity)?.pendingSend(PendingSendInfo(token, comment, amount, fee, outgoingAddress))
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
        feeSeekBar.setOnSeekBarChangeListener(null)
        feeContainer.setOnLongClickListener(null)
        advancedTitle.setOnClickListener(null)
        btnExpandAdvanced.setOnClickListener(null)
        editAddressTitle.setOnClickListener(null)
        btnExpandEditAddress.setOnClickListener(null)
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
