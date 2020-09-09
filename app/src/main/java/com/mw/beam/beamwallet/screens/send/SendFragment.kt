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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.InputFilterMinMax
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.addresses.AddressPagerType
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter
import com.mw.beam.beamwallet.screens.addresses.AddressesPagerAdapter
import com.mw.beam.beamwallet.screens.addresses.Tab
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressFragment
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_send.*
import android.text.InputType
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_send.advancedContainer
import kotlinx.android.synthetic.main.fragment_send.advancedGroup
import kotlinx.android.synthetic.main.fragment_send.amount
import kotlinx.android.synthetic.main.fragment_send.btnChangeAddress
import kotlinx.android.synthetic.main.fragment_send.btnExpandAdvanced
import kotlinx.android.synthetic.main.fragment_send.btnExpandEditAddress
import kotlinx.android.synthetic.main.fragment_send.comment
import kotlinx.android.synthetic.main.fragment_send.editAddressContainer
import kotlinx.android.synthetic.main.fragment_send.editAddressGroup
import kotlinx.android.synthetic.main.fragment_send.tagAction
import kotlinx.android.synthetic.main.fragment_send.tags
import kotlinx.android.synthetic.main.fragment_send.token
import android.graphics.Typeface
import android.util.Log
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.views.*
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_send.secondAvailableSum
import org.jetbrains.anko.withAlpha

/**
 *  11/13/18.
 */
class SendFragment : BaseFragment<SendPresenter>(), SendContract.View {
    private var searchAddressViewDY = 0f
    private lateinit var pagerAdapter: AddressesPagerAdapter
    private var minFee = 0
    private var maxFee = 0
    private var isFirstEditExpand = true
    private var isFirstAdvExpand = true
    private var address = ""
    private var isPaste = false
    private var ignoreWatcher = false

    private val tokenWatcher: TextWatcher = object : PasteEditTextWatcher {
        override fun onPaste() {
            Log.e("TEXT", "onPaste")

            isPaste = true
            ignoreWatcher = true
            token.setTypeface(null,Typeface.NORMAL)
            presenter?.onPaste()
            checkAvailable()
        }

        override fun afterTextChanged(rawToken: Editable?) {
            Log.e("TEXT", "afterTextChanged")

            if(presenter?.state?.isMaxPrivacyRequested == true) {
                presenter?.state?.isMaxPrivacyRequested = false
                presenter?.state?.isMaxPrivacy = false
                setMaxPrivacyRequested(false)
                setMaxPrivacy(false)
            }


            if (!ignoreWatcher) {
                showTokenButton.visibility = View.GONE
                presenter?.onTokenChanged(rawToken.toString())

                if(token.text.toString().isEmpty()) {
                    token.setTypeface(null,Typeface.ITALIC)
                }
                else {
                    token.setTypeface(null,Typeface.NORMAL)
                }
            }

            if(isPaste) {
                isPaste = false
                val enteredAddress = token.text.toString()
                onTrimAddress()
                presenter?.onTokenChanged(enteredAddress)
                handleAddressSuggestions(null)
                requestFocusToAmount()
            }
            else if (!ignoreWatcher) {
                address = token.toString()
            }

            Handler().postDelayed({ contentScrollView?.smoothScrollTo(0, 0) }, 50)
        }
    }

    private val labelWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onLabelAddressChanged(s?.toString() ?: "")
        }
    }

    private val amountWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            presenter?.onAmountChanged()
        }
    }

    private val commentWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if(comment.text.toString().isEmpty()) {
                comment.setTypeface(null,Typeface.ITALIC)
            }
            else {
                comment.setTypeface(null,Typeface.NORMAL)
            }
        }
    }

    private val changeAddressCallback = object : ChangeAddressCallback {
        override fun onChangeAddress(walletAddress: WalletAddress) {
            presenter?.onAddressChanged(walletAddress)
            ChangeAddressFragment.callback = null
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

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isOpenSearchView()) {
                handleAddressSuggestions(null)
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun isOpenSearchView(): Boolean {
        return searchContainer?.height ?: 0 > 0
    }

    private fun checkAvailable() {
        if (AppManager.instance.wallet?.isToken(address) == false /*&& AppManager.instance.isValidAddress(address)*/) {
            maxPrivacyGroup.visibility = View.GONE
//            maxPrivacySwitch.isEnabled = false
//            maxPrivacySwitch.alpha = 0.2f
//            maxPrivacySwitch.isChecked = false
//            maxPrivacySwitch.visibility = View.VISIBLE
//            maxPrivacyDescription.text = getString(R.string.address_not_supported_max_privacy)
//            maxPrivacyDescription.visibility = View.VISIBLE
        }
        else {
            maxPrivacyGroup.visibility = View.VISIBLE
            maxPrivacyDescription.text = getString(R.string.max_privacy_text)
            if (maxPrivacySwitch.isChecked) {
                maxPrivacyDescription.visibility = View.VISIBLE
            }
            else {
                maxPrivacyDescription.visibility = View.GONE
            }
            maxPrivacySwitch.isEnabled = true
            maxPrivacySwitch.alpha = 1.0f
        }
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

    override fun getToken(): String = address
    override fun getComment(): String? = comment.text.toString()
    override fun getFee(): Long {
        val progress = feeSeekBar.progress.toLong() + minFee.toLong()
        return if (progress < 0) 0 else progress
    }


    @SuppressLint("SetTextI18n")
    override fun init(defaultFee: Int, max: Int) {
        maxFee = max

        if(address.isEmpty()) {
            token.setTypeface(null,Typeface.ITALIC)
        }
        else {
            token.setTypeface(null,Typeface.NORMAL)
        }

        if(comment.text.toString().isEmpty()) {
            comment.setTypeface(null,Typeface.ITALIC)
        }
        else {
            comment.setTypeface(null,Typeface.NORMAL)
        }

        setHasOptionsMenu(true)

        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
        maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"

        feeSeekBar.progress = 0
        updateFeeValue(defaultFee)

        ViewCompat.requestApplyInsets(contentScrollView)
        contentScrollView.smoothScrollTo(0, 0)

        pagerAdapter = AddressesPagerAdapter(context!!, object : AddressesAdapter.OnItemClickListener {
            override fun onItemClick(item: WalletAddress) {
                isPaste = true
                presenter?.onSelectAddress(item)
            }
        },null, { presenter?.repository?.getAddressTags(it) ?: listOf() }, AddressPagerType.SMALL)

        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)

        var scrollStartY = 0f
        var isFirstHideFrame = true
        pagerAdapter.setOnTouchListener(View.OnTouchListener { _, event ->
            if ((pagerAdapter.findFirstCompletelyVisibleItemPosition(pager.currentItem) <= 0 && (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP) && scrollStartY < event.y) || (searchContainer.layoutParams as ConstraintLayout.LayoutParams).topMargin > calculateDefaultMargin()) {
                if (isFirstHideFrame && event.action == MotionEvent.ACTION_MOVE) {
                    searchAddressViewDY = (searchContainer.layoutParams as ConstraintLayout.LayoutParams).topMargin - event.rawY
                }
                isFirstHideFrame = false
                handleMotionAction(event, false)
            } else {
                isFirstHideFrame = true
            }

            scrollStartY = event.y

            false
        })

        secondMinFeeValue.text = minFee.toLong().convertToCurrencyGrothString()
        secondMaxFeeValue.text = maxFee.toLong().convertToCurrencyGrothString()

    }

    override fun setMaxPrivacy(value: Boolean) {
        if(maxPrivacySwitch.isChecked != value) {
            maxPrivacySwitch.isChecked = value

            if (value) {
                maxPrivacyDescription.visibility = View.VISIBLE
            }
            else{
                maxPrivacyDescription.visibility = View.GONE
            }

            if(!getToken().isNullOrEmpty()) {
                hasErrors(presenter?.state?.walletStatus?.available ?: 0, presenter?.state?.privacyMode ?: false)
            }
        }
    }

    override fun setMaxPrivacyRequested(value: Boolean) {
        if (value) {
            maxPrivacySwitch.visibility = View.GONE
            maxPrivacyTitle.text = getString(R.string.max_privacy_requested_title)
        }
        else{
            maxPrivacySwitch.visibility = View.VISIBLE
            maxPrivacyTitle.text = getString(R.string.max_privacy_title)
        }
    }

    override fun updateMaxPrivacyCount(count: Int) {
        AppActivity.self.runOnUiThread {
            if(maxPrivacySwitch!=null) {
                if (count != -1 && maxPrivacySwitch.visibility == View.GONE && maxPrivacyTitle.text == getString(R.string.max_privacy_requested_title)) {
                    val text = getString(R.string.max_privacy_requested_title) + ".\n" + getString(R.string.offline_transactions) + ": " +
                            count + "/" + AppManager.instance.maxOfflineCount
                    maxPrivacyTitle.text = text
                }
            }
        }
    }

    override fun setupMaxFee(max: Int, min:Int) {
        maxFee = max
        minFee = min
        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
        secondMinFeeValue.text = minFee.toLong().convertToCurrencyGrothString()

        secondMaxFeeValue.text = maxFee.toLong().convertToCurrencyGrothString()
        maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"
    }

    @SuppressLint("SetTextI18n")
    override fun setupMinFee(fee: Int) {
        minFee = fee

        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
        secondMinFeeValue.text = minFee.toLong().convertToCurrencyGrothString()
    }

    override fun showMinFeeError() {
        showAlert(
                message = "",
                btnConfirmText = "",
                onConfirm = {}
        )
    }

    override fun requestFocusToAmount() {
        if(!amount.isFocused) {
            amount.requestFocus()
            showKeyboard()
        }
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.sent_color)
    }

    private fun handleMotionAction(event: MotionEvent, returnValue: Boolean = true): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                searchAddressViewDY = (searchContainer.layoutParams as ConstraintLayout.LayoutParams).topMargin - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val newMargin = if ((searchAddressViewDY + event.rawY).toInt() < calculateDefaultMargin()) calculateDefaultMargin() else (searchAddressViewDY + event.rawY).toInt()
                searchContainer.layoutParams = (searchContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = newMargin
                }
            }
            MotionEvent.ACTION_UP -> {
                val params = searchContainer.layoutParams as ConstraintLayout.LayoutParams
                if (params.topMargin - calculateDefaultMargin() > searchContainer.height * 0.66) {
                    handleAddressSuggestions(null)
                } else {
                    beginTransaction(true)
                    searchContainer.layoutParams = (params).apply {
                        topMargin = calculateDefaultMargin()
                    }
                }
            }
        }
        return returnValue
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

        scanQR.setOnClickListener {
            presenter?.onScanQrPressed()
        }

        maxPrivacySwitch.setOnClickListener {
            presenter?.onMaxPrivacy(maxPrivacySwitch.isChecked)
            hasErrors(presenter?.state?.walletStatus?.available ?: 0, presenter?.state?.privacyMode ?: false)
        }

        permanentOutSwitch.setOnClickListener {
            presenter?.onExpirePeriodChanged(when (permanentOutSwitch.isChecked) {
                true -> ExpirePeriod.NEVER
                else -> ExpirePeriod.DAY
            })

        }

        showTokenButton.setOnClickListener {
            presenter?.showTokenFragmentPressed()
        }

        addressName.addTextChangedListener(labelWatcher)

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

        token.imeOptions = EditorInfo.IME_ACTION_NEXT
        token.setRawInputType(InputType.TYPE_CLASS_TEXT)
        token.addListener(tokenWatcher)
        token.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                contentScrollView.smoothScrollTo(0, 0)
                presenter?.onTokenChanged(token.text.toString())
            } else {
                handleAddressSuggestions(null)

                if (!AppManager.instance.isValidAddress(getToken())) {
                    setAddressError(getString(R.string.invalid_address))
                }
                else if(AppManager.instance.isToken(getToken())) {
                    val params = AppManager.instance.wallet!!.getTransactionParameters(getToken(), false)
                    if(AppManager.instance.isMyAddress(params.address) && maxPrivacySwitch.isChecked) {
                        setAddressError(getString(R.string.cant_sent_max_to_my_address))
                        contentScrollView?.smoothScrollTo(0, 0)
                    }
                    else if(params.versionError) {
                        setAddressError("This address generated by newer Beam library version ${params.version}. Your version is: ${Api.getLibVersion()}. Please, check for updates.")
                        contentScrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        }
        token.setOnClickListener {
            contentScrollView.smoothScrollTo(0, 0)
            presenter?.onTokenChanged(token.text.toString())
        }

        comment.addTextChangedListener(commentWatcher)

        feeSeekBar.setOnSeekBarChangeListener(onFeeChangeListener)

        advancedContainer.setOnClickListener {
            presenter?.onAdvancedPressed()
        }

        editAddressContainer.setOnClickListener {
            presenter?.onEditAddressPressed()
        }

        btnChangeAddress.setOnClickListener {
            presenter?.onChangeAddressPressed()
        }

        searchContainer.setOnTouchListener { _, event ->
            handleMotionAction(event)
        }

        tabLayout.setOnTouchListener { _, event ->
            handleMotionAction(event)
        }

        tagAction.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        contentScrollView.overScrollMode = ScrollView.OVER_SCROLL_NEVER
        contentScrollView.setOnTouchListener { _, _ -> isOpenSearchView() }

        if(App.isDarkMode) {
            addressContainer.setBackgroundColor(context!!.getColor(R.color.colorPrimary_dark).withAlpha(95))
        }
        else{
            addressContainer.setBackgroundColor(context!!.getColor(R.color.colorPrimary).withAlpha(95))
        }
    }

    override fun onTrimAddress() {
        ignoreWatcher = true
        val enteredAddress = token.text.toString()
        if(AppManager.instance.isValidAddress(enteredAddress)) {
            address = enteredAddress
            val trim = enteredAddress.trimAddress()
            token.setText(trim)
            token.setSelection(token.text?.length ?: 0)
            showTokenButton.visibility = View.VISIBLE
        }
        else {
            showTokenButton.visibility = View.GONE
        }
        ignoreWatcher = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()

        onBackPressedCallback.isEnabled = true

        if(App.isNeedOpenScanner) {
            App.isNeedOpenScanner = false
            presenter?.onScanQrPressed()
        }
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun onHideKeyboard() {
        super.onHideKeyboard()
        if (amount.isFocused) {
            presenter?.onAmountUnfocused()
        }
    }

    private fun calculateDefaultMargin(): Int {
//        var offset = 60
//        if (contactName.visibility == View.VISIBLE) {
//            offset = 120
//        }
        return ScreenHelper.dpToPx(context, 110) //addressContainer.height - offset
    }

    @SuppressLint("InflateParams", "StringFormatInvalid")
    override fun showFeeDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_send_fee, null)

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dialog?.dismiss()
        }

        val secondAvailableSum = view.findViewById<TextView>(R.id.secondAvailableSum)
        secondAvailableSum.text = getFee().convertToCurrencyGrothString()

        val feeEditText = view.findViewById<AppCompatEditText>(R.id.feeEditText)
        feeEditText.setText(getFee().toString())
        feeEditText.filters = arrayOf(InputFilterMinMax(0, Int.MAX_VALUE))
        feeEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                secondAvailableSum.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.feeError)?.visibility = View.GONE

                val rawFee = feeEditText.text?.toString()
                val fee = rawFee?.toLongOrNull() ?: 0
                secondAvailableSum.text = fee.convertToCurrencyGrothString()

                secondAvailableSum.text = fee.convertToCurrencyGrothString()
                secondMaxFeeValue.text = maxFee.toLong().convertToCurrencyGrothString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        view.findViewById<BeamButton>(R.id.btnSave).setOnClickListener {
            val rawFee = feeEditText.text?.toString()
            val fee = rawFee?.toLongOrNull() ?: 0
            if (fee >= minFee) {

                if(fee > presenter!!.MAX_FEE) {
                    maxFee = fee.toInt()
                    secondMaxFeeValue.text = maxFee.toLong().convertToCurrencyGrothString()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onScannedQR(IntentIntegrator.parseActivityResult(resultCode, data).contents, true)
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

        beginTransaction()

        val availableVisibility = if (isEnable) View.GONE else View.VISIBLE
        availableTitle.visibility = availableVisibility
        availableSum.visibility = availableVisibility
        secondAvailableSum.visibility = availableVisibility
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
        params.horizontalBias = if (progress < 0) 0f else progress.toFloat() / feeSeekBar.max

        val fee = if (progress < 0) 0 else progress

        feeProgressValue.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"
        feeProgressValue.layoutParams = params

        val feeString = "(${if (fee > 0) "+" else ""}$fee ${getString(R.string.currency_groth).toUpperCase()} ${getString(R.string.transaction_fee).toLowerCase()})"
        val second = getAmount().convertToCurrencyString()
        if(second!=null) {
            usedFee.text = "$second $feeString"
        }
        else {
            usedFee.text = feeString
        }
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
        clearAddressError()

        if (!AppManager.instance.isValidAddress(getToken())) {
            hasErrors = true
            setAddressError(getString(R.string.invalid_address))
            contentScrollView?.smoothScrollTo(0, 0)
        }
        else if(AppManager.instance.isToken(getToken())) {
            val params = AppManager.instance.wallet!!.getTransactionParameters(getToken(), false)
            if(AppManager.instance.isMyAddress(params.address) && maxPrivacySwitch.isChecked) {
                hasErrors = true
                setAddressError(getString(R.string.cant_sent_max_to_my_address))
                contentScrollView?.smoothScrollTo(0, 0)
            }
        }

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

    @SuppressLint("SetTextI18n")
    override fun configOutgoingAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        outgoingAddressTitle.text = "${getString(R.string.outgoing_token).toUpperCase()}${if (isGenerated) " (${getString(R.string.auto_generated).toLowerCase()})" else ""}"
        outgoingAddress.text = walletAddress.walletID

        addressName.setText(walletAddress.label)

        permanentOutSwitch.isChecked = walletAddress.duration == 0L
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        if (isFirstAdvExpand) {
            isFirstAdvExpand = false
            animateDropDownIcon(btnExpandAdvanced, expand)
            advancedGroup.visibility = if (expand) View.VISIBLE else View.GONE
        }
        else{
            animateDropDownIcon(btnExpandAdvanced, expand)
            beginTransaction(true)

            if(expand) {
                advancedGroup.visible(true)
            }
            else{
                advancedGroup.gone(true)
            }
        }
    }

    override fun handleExpandEditAddress(expand: Boolean) {
        if (isFirstEditExpand) {
            isFirstEditExpand = false
            animateDropDownIcon(btnExpandEditAddress, expand)
            editAddressGroup.visibility = if (expand) View.VISIBLE else View.GONE
        }
        else{
            animateDropDownIcon(btnExpandEditAddress, expand)
            beginTransaction(true)


            if(expand) {
                editAddressGroup.visible(true)
            }
            else{
                editAddressGroup.gone(true)
            }
        }
    }

    override fun setTags(currentTags: List<Tag>) {
        if (currentTags.count()==0) {
            tags.text = getString(R.string.none)
        }
        else{
            tags.text = currentTags.createSpannableString(context!!)
        }
    }

    override fun setupTagAction(isEmptyTags: Boolean) {
        val resId = if (isEmptyTags) R.drawable.ic_add_tag else R.drawable.ic_edit_tag
        val drawable = ContextCompat.getDrawable(context!!, resId)
        tagAction.setImageDrawable(drawable)
    }

    override fun showCreateTagDialog() {
        showAlert(
                getString(R.string.dialog_empty_tags_message),
                getString(R.string.create_tag),
                { presenter?.onCreateNewTagPressed() },
                getString(R.string.tag_list_is_empty),
                getString(R.string.cancel)
        )
    }

    @SuppressLint("InflateParams")
    override fun showTagsDialog(selectedTags: List<Tag>) {
        BottomSheetDialog(context!!, R.style.common_bottom_sheet_style).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.tags_bottom_sheet, null)
            setContentView(view)

            val tagAdapter = TagAdapter { presenter?.onSelectTags(it) }

            val tagList = view.findViewById<RecyclerView>(R.id.tagList)
            val btnBottomSheetClose = view.findViewById<ImageView>(R.id.btnBottomSheetClose)

            tagList.layoutManager = LinearLayoutManager(context)
            tagList.adapter = tagAdapter

            tagAdapter.setSelectedTags(selectedTags)

            btnBottomSheetClose.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    override fun showAddNewCategory() {
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToEditCategoryFragment())
    }

    override fun showTokenFragment() {
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToShowTokenFragment(address, false))
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

    override fun showChangeAddressFragment(generatedAddress: WalletAddress?) {
        ChangeAddressFragment.callback = changeAddressCallback
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToChangeAddressFragment(generatedAddress = generatedAddress))
    }

    override fun setAddress(address: String) {
        token.setText(address)
        token.setSelection(token.text?.length ?: 0)

        if(token.text.toString().isEmpty()) {
            token.setTypeface(null,Typeface.ITALIC)
        }
        else {
            token.setTypeface(null,Typeface.NORMAL)
        }

        if(!address.contains("...")) {
            onTrimAddress()
        }
    }

    override fun setAmount(amount: Double) {
        this.amount.setText(amount.convertToBeamString())
        this.amount.setSelection(this.amount.text?.length ?: 0)
    }

    override fun setComment(comment: String) {
        this.comment.setText(comment)
        this.comment.setSelection(this.comment.text?.length ?: 0)
        if(this.comment.text.toString().isEmpty()) {
            this.comment.setTypeface(null,Typeface.ITALIC)
        }
        else {
            this.comment.setTypeface(null,Typeface.NORMAL)
        }
    }

    override fun showCantSendToExpiredError() {
        showAlert(getString(R.string.send_error_expired_address), getString(R.string.ok), {})
    }

    override fun handleAddressSuggestions(addresses: List<WalletAddress>?, showSuggestions: Boolean) {
        pagerAdapter.setData(Tab.ACTIVE, addresses?.filter { !it.isContact } ?: listOf())
        pagerAdapter.setData(Tab.CONTACTS, addresses?.filter { it.isContact } ?: listOf())

        if (!showSuggestions) return

        Handler().postDelayed({
            if (context == null) return@postDelayed

            beginTransaction(true)

//            val colorId = if (addresses == null) R.color.colorPrimary else android.R.color.transparent
//            addressContainer?.setBackgroundColor(ContextCompat.getColor(context!!, colorId))

            val params = searchContainer?.layoutParams as? ConstraintLayout.LayoutParams
            params?.topMargin = calculateDefaultMargin()
            if (addresses == null) {
                params?.topToBottom = -1
            } else {
                params?.topToBottom = R.id.toolbarLayout
            }
            searchContainer?.layoutParams = params
        }, 25)
    }

    private fun beginTransaction(isLongAnimation: Boolean = false) {
        if (sendRootView != null) {
            if (isLongAnimation) {
                TransitionManager.beginDelayedTransition(sendRootView, AutoTransition().apply {
                    duration = 300
                    excludeChildren(pager, true)
                })
            } else {
                TransitionManager.beginDelayedTransition(sendRootView)
            }
        }
    }

    private fun setAddressError(error: String) {
        tokenError.visibility = View.VISIBLE
        tokenError.text = error
        showTokenButton.visibility = View.GONE

        contactCategory.visibility = View.GONE
        contactIcon.visibility = View.GONE
        contactName.visibility = View.GONE

        val value = ScreenHelper.dpToPx(context, 16)

        val params = maxPrivacyTitle.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(value,value + value + value, value, 0)
        maxPrivacyTitle.layoutParams = params
    }

    override fun setSendContact(walletAddress: WalletAddress?, tags: List<Tag>) {
        contactCategory.visibility = if (tags.isEmpty()) View.GONE else View.VISIBLE
        contactIcon.visibility = if (walletAddress != null || tags.isNotEmpty()) View.VISIBLE else View.GONE
        contactName.visibility = if (walletAddress == null) View.GONE else View.VISIBLE

        walletAddress?.label?.let {
            contactName.text = if (it.isBlank()) getString(R.string.no_name) else it
        }

        contactCategory.text = tags.createSpannableString(context!!)
        contactName.setTypeface(null,Typeface.NORMAL)

        if (walletAddress == null) {
            val token = getToken()
            if(contactName.visibility == View.GONE) {
                if(!token.isNullOrEmpty() && AppManager.instance.isValidAddress(token)) {
                    presenter?.onScannedQR(token, false)
                    var params = AppManager.instance.wallet?.getTransactionParameters(token, true)
                    if(params!=null) {
                        contactName.visibility = View.VISIBLE
                        contactName.setTypeface(null,Typeface.ITALIC)
                        contactIcon.visibility = View.GONE

                        if(params.isPermanentAddress) {
                            contactName.text = getString(R.string.perm_token)
                        }
                        else {
                            contactName.text = getString(R.string.one_time_expire_text)
                        }
                    }
                }
            }
        }

        checkAvailable()
    }


    override fun clearAddressError() {
        tokenError.visibility = View.INVISIBLE

        val value = ScreenHelper.dpToPx(context, 16)

        val params = maxPrivacyTitle.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(value,value, value, 0)
        maxPrivacyTitle.layoutParams = params
    }

    override fun clearToken(clearedToken: String?) {
        token.setText(clearedToken)
        token.setSelection(token.text?.length ?: 0)
    }

    override fun clearErrors() {
        amountError.visibility = View.GONE
        amount.setTextColor(ContextCompat.getColor(context!!, R.color.sent_color))
        amount.isStateNormal = true
        updateFeeTransactionVisibility()
    }

    override fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean) {
        configPrivacyStatus(isEnablePrivacyMode)

        amount.text = null
        comment.text = null

        feeSeekBar.progress = defaultFee - minFee

        updateFeeValue(defaultFee)

    }

    override fun updateFeeViews(clearAmountFocus: Boolean) {
        amount.setTextColor(ContextCompat.getColorStateList(context!!, R.color.sent_color))
        updateFeeValue(feeSeekBar.progress+minFee, clearAmountFocus)
    }

    override fun showConfirmTransaction(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long, maxPrivacy: Boolean) {
        var isOffline = false
        val params = AppManager.instance.wallet?.getTransactionParameters(token, false)
        if(params?.isOffline == true) {
            isOffline = true
        }
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToSendConfirmationFragment(token, outgoingAddress, amount, fee, comment, maxPrivacy, isOffline))
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
        token.removeListener(tokenWatcher)
        comment.removeTextChangedListener(commentWatcher)
        addressName.removeTextChangedListener(tokenWatcher)
        amount.removeTextChangedListener(amountWatcher)
        amount.filters = emptyArray()
        amount.onFocusChangeListener = null
        feeSeekBar.setOnSeekBarChangeListener(null)
        feeContainer.setOnLongClickListener(null)
        advancedContainer.setOnClickListener(null)
        editAddressContainer.setOnClickListener(null)
        searchContainer.setOnTouchListener(null)
        tabLayout.setOnTouchListener(null)
        contentScrollView.setOnTouchListener(null)
        token.setOnClickListener(null)
        tagAction.setOnClickListener(null)
        maxPrivacySwitch.setOnClickListener(null)
        showTokenButton.setOnClickListener(null)
        permanentOutSwitch.setOnClickListener(null)
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
        return SendPresenter(this, SendRepository(), SendState())
    }
}
