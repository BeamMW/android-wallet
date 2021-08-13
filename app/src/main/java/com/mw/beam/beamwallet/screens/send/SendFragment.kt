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
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.*
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.views.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.InputFilterMinMax
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.addresses.AddressPagerType
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter
import com.mw.beam.beamwallet.screens.addresses.AddressesPagerAdapter
import com.mw.beam.beamwallet.screens.addresses.Tab
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressFragment
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity

import kotlinx.android.synthetic.main.fragment_send.*

import java.text.NumberFormat
import java.util.*

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
    private var isVisibleComment = false
    private var isOffline = false
    private var isMaxPrivacy = false
    private var typeAddress:BMAddressType? = null

    private val tokenWatcher: TextWatcher = object : PasteEditTextWatcher {
        override fun onPaste() {
            isPaste = true
            ignoreWatcher = true
            token.setTypeface(null,Typeface.NORMAL)
            presenter?.onPaste()
        }

        override fun afterTextChanged(rawToken: Editable?) {
            if (!ignoreWatcher) {
                showTokenButton.visibility = View.GONE
                tokenDivider.setPadding(0, 0,ScreenHelper.dpToPx(context, 20),0)
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
                setSegmentButtons()

                currency.text = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName
                presenter?.onAmountChanged()
                updateAvailable(AssetManager.instance.getAvailable(presenter?.assetId ?: 0))
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
            if(txComment.text.toString().isEmpty()) {
                txComment.setTypeface(null,Typeface.ITALIC)
            }
            else {
                txComment.setTypeface(null,Typeface.NORMAL)
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
            updateFeeValue(p, false)
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


    override fun onControllerGetContentLayoutId() = R.layout.fragment_send
    override fun getToolbarTitle(): String = getString(R.string.send)

    override fun getAddressFromArguments(): String? {
        return SendFragmentArgs.fromBundle(requireArguments()).address
    }

    override fun getAmountFromArguments(): Long {
        return SendFragmentArgs.fromBundle(requireArguments()).amount
    }

    override fun getAmountText(): String {
        return amount.text.toString()
    }

    override fun getAmount(): Double {
        return try {
            amount.text.toString().toDouble()
        } catch (ex:Exception) {
            0.0
        }
    }

    private fun getRealAmount(): Double = try {
        amount.text.toString().toDouble()
    } catch (e: Exception) {
        0.0
    }

    override fun getToken(): String = address
    override fun getComment(): String? = txComment.text.toString()
    override fun getFee(): Long {
        val progress = feeSeekBar.progress.toLong() + minFee.toLong()
        return if (progress < 0) 0 else progress
    }

    override fun isOffline(): Boolean {
        if(transactionTypeLayout.visibility == View.VISIBLE && isOffline) {
            return true
        }
        return false
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun init(defaultFee: Int, max: Int) {
        maxFee = max

        if(address.isEmpty()) {
            token.setTypeface(null,Typeface.ITALIC)
        }
        else {
            token.setTypeface(null,Typeface.NORMAL)
        }

        if(txComment.text.toString().isEmpty()) {
            txComment.setTypeface(null,Typeface.ITALIC)
        }
        else {
            txComment.setTypeface(null,Typeface.NORMAL)
        }

        setHasOptionsMenu(true)

        feeSeekBar.max = maxFee - minFee

        minFeeValue.text = "$minFee ${getString(R.string.currency_groth).toUpperCase()}"
        maxFeeValue.text = "$maxFee ${getString(R.string.currency_groth).toUpperCase()}"

        feeSeekBar.progress = 0
        updateFeeValue(defaultFee)

        ViewCompat.requestApplyInsets(contentScrollView)
        contentScrollView.smoothScrollTo(0, 0)

        pagerAdapter = AddressesPagerAdapter(requireContext(), object : AddressesAdapter.OnItemClickListener {
            override fun onItemClick(item: WalletAddress) {
                isPaste = true
                presenter?.onSelectAddress(item)
            }
        },null, AddressPagerType.SMALL)
        pagerAdapter.displayAddressType = true

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

        handleExpandComment(isVisibleComment)

        setSegmentButtons()

        buttonGroupDraggable.onPositionChangedListener = SegmentedButtonGroup.OnPositionChangedListener {
            if (it == 0) {
                isOffline = false
                addressTypeLabel.text = getString(R.string.regular_online_address) + "."
                setSegmentButtons()
                presenter?.requestFee()
            } else {
                isOffline = true
                updateMaxPrivacyCount(presenter?.state?.maxPrivacyCount ?: 1)
                setSegmentButtons()
                presenter?.requestFee()
            }
        }
    }

    private fun setSegmentButtons() {
        if(typeAddress == BMAddressType.BMAddressTypeOfflinePublic) {
            sendDescription.text = resources.getString(R.string.min_fee_offline)
        }
        else if(isMaxPrivacy) {
            sendDescription.text = resources.getString(R.string.send_notice_max_privacy)
        }
        else if(!isOffline) {
            sendDescription.text = resources.getString(R.string.confirmation_send_description)
        }
        else {
            sendDescription.text = resources.getString(R.string.send_offline_hint)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun updateMaxPrivacyCount(count: Int) {
        if (isOffline) {
            val left = getString(R.string.transactions_left, count.toString())
            var result = getString(R.string.regular_offline_address) + ": " + left + "."
            if (count <=3) {
                result += " " + getString(R.string.transactions_left_hint)
            }
            addressTypeLabel.text = result
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
        return if (App.isDarkMode) {
            ContextCompat.getColor(requireContext(), R.color.send_toolbar_color_dark)
        }
        else {
            ContextCompat.getColor(requireContext(), R.color.send_toolbar_color)
        }
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

    @SuppressLint("RestrictedApi")
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

        txCommentContainer.setOnClickListener {
            isVisibleComment = !isVisibleComment
            handleExpandComment(isVisibleComment)
        }

        permanentOutSwitch.setOnClickListener {
            presenter?.onExpirePeriodChanged(when (permanentOutSwitch.isChecked) {
                true -> ExpirePeriod.NEVER
                else -> ExpirePeriod.DAY
            })

            if (permanentOutSwitch.isChecked) {
                permanentOutText.text =  getString(R.string.perm_out_address_text)
            }
            else {
                permanentOutText.text = getString(R.string.address_expire_after_2h)
            }
        }

        showTokenButton.text = getString(R.string.address_details).toLowerCase()
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
                clearAddressError()

                if (!AppManager.instance.isValidAddress(getToken())) {
                    setAddressError(getString(R.string.invalid_address))
                }
                else if(AppManager.instance.isToken(getToken())) {
                    setAddressType(getToken())
                }
            }
        }
        token.setOnClickListener {
            contentScrollView.smoothScrollTo(0, 0)
            presenter?.onTokenChanged(token.text.toString())
        }

        txComment.addTextChangedListener(commentWatcher)

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

        contentScrollView.overScrollMode = ScrollView.OVER_SCROLL_NEVER
        contentScrollView.setOnTouchListener { _, _ -> isOpenSearchView() }

//        if(App.isDarkMode) {
//            addressContainer.setBackgroundColor(requireContext().getColor(R.color.colorPrimary_dark).withAlpha(95))
//        }
//        else{
//            addressContainer.setBackgroundColor(requireContext().getColor(R.color.colorPrimary).withAlpha(95))
//        }

        if(AssetManager.instance.assets.size != 1) {
            currencyLayout
                .setOnClickListener {
                    animateDropDownIcon(btnExpandCurrency, true)
                    val menu = PopupMenu(requireContext(), currencyLayout, Gravity.END, R.attr.listPopupWindowStyle, R.style.popupOverflowMenu)
                    menu.setOnDismissListener {
                        animateDropDownIcon(btnExpandCurrency, false)
                    }
                    menu.gravity = Gravity.END;

                    AssetManager.instance.assets.forEach {
                        var name = it.unitName
                        if (name.length > 8) {
                            name = name.substring(0,8) + "..."
                        }
                        val sb = SpannableString(name)
                        if (it.assetId == presenter?.assetId) {
                            sb.setSpan(StyleSpan(Typeface.BOLD), 0, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            sb.setSpan(ForegroundColorSpan(requireContext().getColor(R.color.colorAccent)), 0, sb.length, 0)
                        }
                        else {
                            sb.setSpan(ForegroundColorSpan(Color.WHITE), 0, sb.length, 0)
                        }
                        val item = menu.menu.add(sb)
                        item.setIcon(it.image)
                    }
                    menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val title = item.title.toString().replace("...", "")
                        val asset = AssetManager.instance.getAssetName(title)
                        val assetId = asset?.assetId ?: 0
                        if(presenter?.assetId != assetId) {
                            presenter?.isAllPressed = false
                            amount.setText("")
                        }
                        presenter?.assetId = assetId
                        currency.text = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName
                        presenter?.onAmountChanged()
                        updateAvailable(AssetManager.instance.getAvailable(assetId))
                        updateAvailable(AssetManager.instance.getAvailable(assetId))

                        true
                    })

                    if (menu.menu is MenuBuilder) {
                        (menu.menu as MenuBuilder).setOptionalIconsVisible(true)
                    }
                    menu.show()
                }
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
            tokenDivider.setPadding(0,0,0,0)
        }
        else if(!enteredAddress.contains("...")) {
            showTokenButton.visibility = View.GONE
            tokenDivider.setPadding(0, 0,ScreenHelper.dpToPx(context, 20),0)
        }
        ignoreWatcher = false
        setAddressType(this.address)
    }

    fun handleExpandComment(expand: Boolean) {
        animateDropDownIcon(btnExpandComment, expand)
       // TransitionManager.beginDelayedTransition(contentLayout)
        txCommentGroup.visibility = if (expand) View.VISIBLE else View.GONE

        if (expand) {
            txCommentContainer.setPadding(0,ScreenHelper.dpToPx(context, 20),0,0)
        }
        else {
            txCommentContainer.setPadding(0,ScreenHelper.dpToPx(context, 20),0,ScreenHelper.dpToPx(context, 20))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        animateDropDownIcon(btnExpandCurrency, false)

        if (App.isDarkMode) {
            gradientView.setBackgroundResource(R.drawable.send_bg_dark)
        }
        else {
            gradientView.setBackgroundResource(R.drawable.send_bg)
        }
    }

    override fun onStart() {
        super.onStart()

        token.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        onBackPressedCallback.isEnabled = true

        if(App.isNeedOpenScanner) {
            App.isNeedOpenScanner = false
            presenter?.onScanQrPressed()
        }

        if(AssetManager.instance.assets.size <= 1) {
            btnExpandCurrency.visibility = View.GONE
        }

        currency.text = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName
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
                val nf: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
                nf.isGroupingUsed = true

                val s = nf.format(minFee)

                val feeErrorTextView = view.findViewById<TextView>(R.id.feeError)
                if(minFee == 100) {
                    feeErrorTextView?.text = getString(R.string.min_100_groth, s)
                }
                else {
                    feeErrorTextView?.text = getString(R.string.min_fee_error, s)
                }


                feeErrorTextView.visibility = View.VISIBLE
                secondAvailableSum.visibility = View.GONE
            }
        }

        dialog = AlertDialog.Builder(requireContext()).setView(view).show().apply {
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

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val usd = SpannableStringBuilder()
        usd.append("USD")
        usd.setSpan(ForegroundColorSpan(Color.WHITE),
                0, usd.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val btc = SpannableStringBuilder()
        btc.append("BTC")
        btc.setSpan(ForegroundColorSpan(Color.WHITE),
                0, btc.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val beam = SpannableStringBuilder()
        beam.append("BEAM")
        beam.setSpan(ForegroundColorSpan(Color.WHITE),
                0, beam.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        menu.add(0, 3, 0, beam)
        menu.add(0, 1, 0, usd)
        menu.add(0, 2, 0, btc)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            3 -> {
//                presenter?.currency = Currency.Beam
//            }
//            2 -> {
//                presenter?.currency = Currency.Bitcoin
//            }
//            1 -> {
//                presenter?.currency = Currency.Usd
//            }
//        }
        currency.text = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName
        presenter?.onAmountChanged()
        return super.onContextItemSelected(item)
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
        ExchangeManager.instance.isPrivacyMode = isEnable

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
//        if (clearAmountFocus) {
//            amount.clearFocus()
//        }

        val params = feeProgressValue.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias = if (progress < 0) 0f else progress.toFloat() / feeSeekBar.max

        val fee = if (progress < 0) 0 else progress

        feeProgressValue.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"
        feeProgressValue.layoutParams = params

        val second = getRealAmount().convertToGroth().exchangeValueAsset(presenter?.assetId ?: 0, true)
        if (second.isEmpty()) {
            usedFee.visibility = View.GONE
        }
        else {
            usedFee.visibility = View.VISIBLE
        }

        usedFee.text = second
    }

    override fun updateFeeTransactionVisibility() {
//        usedFee.visibility = if ((getAmount() > 0.0) && amountError.visibility == View.GONE) View.VISIBLE else View.GONE
//        if (usedFee.visibility == View.VISIBLE) {
            updateFeeValue(feeSeekBar.progress+minFee, false)
//        }
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
            hasErrors = false
            clearAddressError()

            val params = AppManager.instance.wallet!!.getTransactionParameters(getToken(), false)

            if(params.isMaxPrivacy && !AppManager.instance.canSendToMaxPrivacy(getToken())) {
                hasErrors = true
                setAddressError(AppActivity.self.getString(R.string.cant_send_to_max_privacy))
            }

            else if(params.versionError) {
                hasErrors = false
                setAddressError("This address generated by newer Beam library version ${params.version}. Your version is: ${Api.getLibVersion()}. Please, check for updates.")
                contentScrollView?.smoothScrollTo(0, 0)
            }
        }
        else {
            hasErrors = false
            clearAddressError()
            contentScrollView?.smoothScrollTo(0, 0)
        }

        if (hasAmountError(getAmount().convertToGroth(), getFee(), availableAmount, isEnablePrivacyMode)) {
            hasErrors = true
        }

        return hasErrors
    }

    override fun hasAmountError(amount: Long, fee: Long, availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean {
        var assetName = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName ?: ""
        if (assetName.length > 15) {
            assetName = assetName.substring(0,14) + "..."
        }
        return try {
            when {

                this.amount.text.isNullOrBlank() -> {
                    configAmountError(getString(R.string.send_amount_empty_error))
                    true
                }
                amount == 0L && fee < availableAmount -> {
                    configAmountError(getString(R.string.send_amount_zero_error_asset,amount.convertToAssetStringWithId(presenter?.assetId)))
                    true
                }
                amount > availableAmount && presenter?.assetId != 0 -> {
                    configAmountError(configAmountErrorMessage(((availableAmount - (amount)) * -1).convertToAssetString(assetName), isEnablePrivacyMode))
                    true
                }
                fee > AssetManager.instance.getAvailable(0) && presenter?.assetId != 0 -> {
                    configAmountError(configAmountErrorMessage(((availableAmount - (amount)) * -1).convertToAssetString(assetName), isEnablePrivacyMode))
                    true
                }
                amount + fee > availableAmount && presenter?.assetId == 0 -> {
                    configAmountError(configAmountErrorMessage(((availableAmount - (amount + fee)) * -1).convertToAssetString(assetName), isEnablePrivacyMode))
                    true
                }
                else -> false
            }
        } catch (exception: NumberFormatException) {
            configAmountError(configAmountErrorMessage(amount.convertToAssetString(assetName), isEnablePrivacyMode))
            true
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun configAmountErrorMessage(amountString: String, isEnablePrivacyMode: Boolean): String {
        return if (isEnablePrivacyMode) {
            getString(R.string.insufficient_funds)
        } else {
            if (presenter?.assetId == 0) {
               getString(R.string.send_amount_overflow_error, amountString)
            }
            else {
                val beamsAvailable = AssetManager.instance.getAvailable(0)
                val feeAmount = try {
                    getFee() ?: 0L
                } catch (exception: NumberFormatException) {
                    0L
                }
                if(beamsAvailable < feeAmount) {
                    val left = feeAmount - beamsAvailable
                    getString(R.string.send_amount_overflow_error, left.convertToBeamString())
                }
                else {
                  getString(R.string.send_amount_overflow_error, amountString)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun configOutgoingAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        outgoingAddressTitle.text = "${getString(R.string.outgoing_address).toUpperCase()}${if (isGenerated) " (${getString(R.string.auto_generated).toLowerCase()})" else ""}"
        outgoingAddress.text = walletAddress.id.trimAddress()

        addressName.setText(walletAddress.label)

        permanentOutSwitch.isChecked = walletAddress.duration == 0L

        if (permanentOutSwitch.isChecked) {
            permanentOutText.text =  getString(R.string.perm_out_address_text)
        }
        else {
            permanentOutText.text = getString(R.string.address_expire_after_2h)
        }
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        if (isFirstAdvExpand) {
            isFirstAdvExpand = false
            animateDropDownIcon(btnExpandAdvanced, expand)
            advancedGroup.visibility = if (expand) View.VISIBLE else View.GONE
        }
        else{
            animateDropDownIcon(btnExpandAdvanced, expand)
           // beginTransaction(true)

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
           // beginTransaction(true)


            if(expand) {
                editAddressGroup.visible(true)
            }
            else{
                editAddressGroup.gone(true)
            }
        }
    }

    override fun showTokenFragment() {
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToShowTokenFragment(address,
            receive = false,
            isNewFormat = false,
            name = null
        ))
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
        else {
            setAddressType(this.address)
        }
    }

    override fun setAmount(amount: Double) {
        this.amount.setText(amount.convertToBeamString())
        this.amount.setSelection(this.amount.text?.length ?: 0)
    }

    override fun setComment(comment: String) {
        this.txComment.setText(comment)
        this.txComment.setSelection(this.txComment.text?.length ?: 0)
        if(this.txComment.text.toString().isEmpty()) {
            this.txComment.setTypeface(null,Typeface.ITALIC)
        }
        else {
            this.txComment.setTypeface(null,Typeface.NORMAL)
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
        if (error.contains("by newer Beam library"))
        {
            newVersionTextView.text = error
            newVersionTextView.visibility = View.VISIBLE
            showTokenButton.visibility = View.VISIBLE
            tokenDivider.setPadding(0,0,0,0)
        }
        else {
            tokenError.visibility = View.VISIBLE
            tokenError.text = error
            newVersionTextView.visibility = View.GONE

            if(error.contains("by newer Beam library") || error.contains("Can not sent max privacy")) {
                showTokenButton.visibility = View.VISIBLE
                tokenDivider.setPadding(0,0,0,0)
            }
            else {
                showTokenButton.visibility = View.GONE
                tokenDivider.setPadding(0, 0,ScreenHelper.dpToPx(context, 20),0)
            }
        }

    }

    override fun setSendContact(walletAddress: WalletAddress?) {
        contactCategory.visibility =  View.GONE
        contactIcon.visibility = if (walletAddress != null) View.VISIBLE else View.GONE
        contactName.visibility = if (walletAddress == null) View.GONE else View.VISIBLE

        walletAddress?.label?.let {
            contactName.text = if (it.isBlank()) getString(R.string.no_name) else it
        }

        contactName.setTypeface(null,Typeface.NORMAL)

        if (walletAddress == null) {
            val token = getToken()
            if(contactName.visibility == View.GONE) {
                checkAddress(token)
            }
        }
        else {
            showTokenButton.visibility = View.VISIBLE
            tokenDivider.setPadding(0,0,0,0)
        }
    }

    private fun checkAddress(token: String) {
        if(!token.isNullOrEmpty() && AppManager.instance.isValidAddress(token)) {
            clearAddressError()

            presenter?.onScannedQR(token, false)

            setAddressType(token)
        }
        else {
            addressTypeLabel.visibility = View.GONE
        }
    }

    override fun clearAddressError() {
        tokenError.visibility = View.GONE
    }

    override fun clearToken(clearedToken: String?) {
        token.setText(clearedToken)
        token.setSelection(token.text?.length ?: 0)
    }

    override fun clearErrors() {
//        if(amountError.visibility == View.VISIBLE && amount.text.isNullOrEmpty()) {
//            updateFeeTransactionVisibility()
//            return
//        }

        amountError.visibility = View.GONE
        amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.sent_color))
        amount.isStateNormal = true
        updateFeeTransactionVisibility()
    }

    override fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean) {
        configPrivacyStatus(isEnablePrivacyMode)

        amount.text = null
        txComment.text = null

        feeSeekBar.progress = defaultFee - minFee

        updateFeeValue(defaultFee)

    }

    override fun updateFeeViews(clearAmountFocus: Boolean) {
        amount.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.sent_color))
        updateFeeValue(feeSeekBar.progress+minFee, clearAmountFocus)
    }

    override fun showConfirmTransaction(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        var remainingCount = -1
        if(presenter?.state?.maxPrivacyCount != null) {
            remainingCount = presenter?.state?.maxPrivacyCount!!
        }
        val change = presenter!!.change
        val inputShield = presenter!!.inputShield
        val addressType = presenter?.state?.addressType?.ordinal ?: 0
        var isOfflineTransaction = false
        if(transactionTypeLayout.visibility == View.VISIBLE && isOffline) {
            isOfflineTransaction = true
        }
        findNavController().navigate(SendFragmentDirections.actionSendFragmentToSendConfirmationFragment(token, outgoingAddress, amount, fee, comment, addressType, remainingCount, change, inputShield, isOfflineTransaction, presenter?.assetId ?: 0))
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun updateAvailable(available: Long) {
        btnSendAll.isEnabled = available > 0
        if (available > 0) {
            btnSendAll.alpha = 1.0f
        }
        else {
            btnSendAll.alpha = 0.5f
        }
        availableSum.text = available.convertToAssetStringWithId(presenter?.assetId).toUpperCase()
        secondAvailableSum.text = available.exchangeValueAsset(presenter?.assetId ?: -1)
    }

    override fun isAmountErrorShown(): Boolean {
        return amountError.visibility == View.VISIBLE
    }

    override fun clearListeners() {
        btnFeeKeyboard.setOnClickListener(null)
        btnNext.setOnClickListener(null)
        btnSendAll.setOnClickListener(null)
        token.removeListener(tokenWatcher)
        txComment.removeTextChangedListener(commentWatcher)
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
        showTokenButton.setOnClickListener(null)
        permanentOutSwitch.setOnClickListener(null)
        currencyLayout.setOnClickListener(null)
    }

    private fun configAmountError(errorString: String) {
        amountError.visibility = View.VISIBLE
        amountError.text = errorString
        amount.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.text_color_selector))
        amount.isStateError = true
        updateFeeTransactionVisibility()
        currency.text = AssetManager.instance.getAsset(presenter?.assetId ?: 0)?.unitName
    }

    private fun showAppDetailsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context?.packageName}")
        startActivity(intent)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendPresenter(this, SendRepository(), SendState())
    }

    @SuppressLint("SetTextI18n")
    private fun setAddressType(token: String) {
        isMaxPrivacy = false
        typeAddress = null
        val params = AppManager.instance.wallet?.getTransactionParameters(token, false)
        if(params!=null) {
            typeAddress = params.getAddressType()
            when (params.getAddressType()) {
                BMAddressType.BMAddressTypeMaxPrivacy -> {
                    isMaxPrivacy = true
                    transactionTypeLayout.visibility = View.GONE
                    addressTypeLabel.text = getString(R.string.send_max_privacy_title) + "."
                }
                BMAddressType.BMAddressTypeOfflinePublic -> {
                    transactionTypeLayout.visibility = View.GONE
                    addressTypeLabel.text = getString(R.string.public_offline_address) + "."
                }
                else -> {
                    if (params.isShielded) {
                        transactionTypeLayout.visibility = View.VISIBLE
                    }
                    else {
                        transactionTypeLayout.visibility = View.GONE
                    }

                    if (params.isShielded) {
                        if (isOffline) {
                            updateMaxPrivacyCount(presenter?.state?.maxPrivacyCount ?: 1)
                        }
                        else {
                            addressTypeLabel.text = getString(R.string.regular_online_address) + "."
                        }
                    }
                    else {
                        addressTypeLabel.text = getString(R.string.regular_online_address) + "."
                    }
                }
            }

            addressTypeLabel.visibility = View.VISIBLE

            if(params.versionError) {
                setAddressError("This address generated by newer Beam library version ${params.version}. Your version is: ${Api.getLibVersion()}. Please, check for updates.")
                contentScrollView?.smoothScrollTo(0, 0)
            }
        }
        else {
            transactionTypeLayout.visibility = View.GONE
            addressTypeLabel.visibility = View.GONE
        }

        setSegmentButtons()
    }
}

