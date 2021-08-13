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

package com.mw.beam.beamwallet.screens.receive

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.fragment_receive.amount
import kotlinx.android.synthetic.main.fragment_receive.amountContainer
import kotlinx.android.synthetic.main.fragment_receive.amountTitle
import kotlinx.android.synthetic.main.fragment_receive.btnExpandAdvanced
import kotlinx.android.synthetic.main.fragment_receive.btnExpandComment
import kotlinx.android.synthetic.main.fragment_receive.btnExpandCurrency
import kotlinx.android.synthetic.main.fragment_receive.contentLayout
import kotlinx.android.synthetic.main.fragment_receive.currency
import kotlinx.android.synthetic.main.fragment_receive.currencyLayout
import kotlinx.android.synthetic.main.fragment_receive.secondAvailableSum
import kotlinx.android.synthetic.main.fragment_receive.txComment
import kotlinx.android.synthetic.main.fragment_receive.txCommentContainer
import kotlinx.android.synthetic.main.fragment_receive.txCommentGroup
import kotlinx.android.synthetic.main.fragment_send.*
import java.util.*


class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {

    private val copyTag = "ADDRESS"
    private var sbbsAddress = ""
    private var offlineAddress = ""
    private var maxPrivacyAddress = ""
    private var assetId = -1

    private val amountWatcher: com.mw.beam.beamwallet.core.watchers.TextWatcher = object : com.mw.beam.beamwallet.core.watchers.TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            val amount = getAmount() ?: 0.0
            secondAvailableSum.text = amount.convertToGroth().exchangeValueAsset(assetId)
            presenter?.updateToken()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            presenter?.onBackPressed()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_receive
    override fun getToolbarTitle(): String = getString(R.string.receive)

    override fun getAmountFromArguments(): Long {
        return ReceiveFragmentArgs.fromBundle(requireArguments()).amount
    }

    override fun getWalletAddressFromArguments(): WalletAddress? {
        return ReceiveFragmentArgs.fromBundle(requireArguments()).walletAddress
    }

    override fun getAmount(): Double? = amount.text?.toString()?.toDoubleOrNull()

    override fun getAssetId(): Int {
        if(assetId == -1) {
            assetId = ReceiveFragmentArgs.fromBundle(requireArguments()).assetId
        }
        return assetId
    }

    override fun setAmount(newAmount: Double) {
        amount.setText(newAmount.convertToBeamString())
        secondAvailableSum.text = newAmount.convertToGroth().exchangeValueAsset(assetId)
    }

    override fun getTxComment(): String? {
        return txComment?.text?.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        secondAvailableSum.text = (getAmount() ?: 0.0).convertToGroth().exchangeValueAsset(assetId)
        amount.filters = arrayOf(AmountFilter())
        amountTitle.text = "${getString(R.string.requested_amount).toUpperCase()} (${getString(R.string.optional).toLowerCase()})"
    }


    override fun getStatusBarColor(): Int {
        return if(App.isDarkMode) {
            ContextCompat.getColor(requireContext(), R.color.receive_toolbar_color_dark)
        }
        else {
            ContextCompat.getColor(requireContext(), R.color.receive_toolbar_color)
        }
    }

    override fun updateTokens(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions) {
        sbbsAddress = walletAddress.id
        offlineAddress = walletAddress.tokenOffline
        maxPrivacyAddress = walletAddress.tokenMaxPrivacy

        if (transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            if (!AppManager.instance.isMaxPrivacyEnabled()) {
                addressLabel.text = walletAddress.address.trimAddress()
            }
            else {
                addressLabel.text = walletAddress.tokenOffline.trimAddress()
            }
        }
        else {
            addressLabel.text = walletAddress.tokenMaxPrivacy.trimAddress()
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions){
        nameComment.setText(walletAddress.label)

        if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            switchView.isChecked = false
            addressTitle.text = resources.getString(R.string.address).toUpperCase()
            receiveDescription.text = resources.getString(R.string.receive_description)
            if (!AppManager.instance.isMaxPrivacyEnabled()) {
                addressLabel.text = walletAddress.address.trimAddress()
            }
            else {
                addressLabel.text = walletAddress.tokenOffline.trimAddress()
            }
        }
        else {
            val lockLimit = AppManager.instance.wallet?.getMaxPrivacyLockTimeLimitHours() ?: 0L
            val time = getMaxPrivacyStringValue(lockLimit)
            val description = if (time.isNullOrEmpty()) {
                resources.getString(R.string.receive_notice_max_privacy, getString(R.string.transaction_indefinitely))
            }
            else {
                resources.getString(R.string.receive_notice_max_privacy, getString(R.string.transaction_time, time))
            }
            switchView.isChecked = true
            addressTitle.text = resources.getString(R.string.address).toUpperCase() +
                    " (" + resources.getString(R.string.maximum_anonymity) + ")"
            receiveDescription.text = description
            addressLabel.text = walletAddress.tokenMaxPrivacy.trimAddress()
        }

        AppActivity.self.runOnUiThread {
            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                switcherView.alpha = 0.5f
                maxLabel.alpha = 0.5f
              //  notAvailableLabel.text = getString(R.string.max_privacy_disabled_node)
               // notAvailableLabel.visibility = View.VISIBLE
                switchView.isEnabled = false
                receiveDescription.text = resources.getString(R.string.receive_description_2)
            }
        }
    }

    override fun handleExpandAmount(expand: Boolean) {
        animateDropDownIcon(btnExpandAmount, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        amountGroup.visibility = if (expand) View.VISIBLE else View.GONE

        if (expand) {
            amountContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, 0)
        }
        else {
            amountContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, ScreenHelper.dpToPx(context, 20))
        }
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        animateDropDownIcon(btnExpandAdvanced, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        advacnedGroup.visibility = if (expand) View.VISIBLE else View.GONE
//        if (expand) {
//            advancedCardContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, 0)
//        }
//        else {
//            advancedCardContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, ScreenHelper.dpToPx(context, 20))
//        }
    }

    override fun handleExpandComment(expand: Boolean) {
        animateDropDownIcon(btnExpandComment, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        txCommentGroup.visibility = if (expand) View.VISIBLE else View.GONE

        if (expand) {
            txCommentContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, 0)
        }
        else {
            txCommentContainer.setPadding(0, ScreenHelper.dpToPx(context, 20), 0, ScreenHelper.dpToPx(context, 20))
        }
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 360f else 180f
        val angleTo = if (shouldExpand) 180f else 360f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun copyToken(receiveToken: String) {
        presenter?.state?.wasAddressSaved = true
        copyToClipboard(receiveToken, copyTag)
        showSnackBar(getString(R.string.address_copied_to_clipboard))
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnShareToken.setOnClickListener {
            presenter?.onShareTokenPressed()
        }

        advancedCardContainer.setOnClickListener {
            presenter?.onAdvancedPressed()
        }
        txCommentContainer.setOnClickListener {
            presenter?.onCommentPressed()
        }

        amountContainer.setOnClickListener {
            presenter?.onAmountPressed()
        }

        switchView.setOnClickListener {
            if (switchView.isChecked) {
                presenter?.onMaxPrivacyPressed()
            }
            else {
                presenter?.onRegularPressed()
            }
        }

        amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }

        amount.addTextChangedListener(amountWatcher)

        nameComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter?.setAddressName(nameComment.text.toString())
            }
        })

        showDetailButton.setOnClickListener {
            presenter?.onTokenPressed()
        }

        qrCodeButton.setOnClickListener {
            presenter?.onShowQrPressed()
        }

        copyButton.setOnClickListener {
            presenter?.onCopyPressed()
        }

        if(AssetManager.instance.assets.size != 1) {
            currencyLayout.setOnClickListener {
                animateDropDownIcon(btnExpandCurrency, true)
                val menu = PopupMenu(requireContext(), currencyLayout, Gravity.END, R.attr.listPopupWindowStyle, R.style.popupOverflowMenu)
                menu.gravity = Gravity.END;

                menu.setOnDismissListener {
                    animateDropDownIcon(btnExpandCurrency, false)
                }
                AssetManager.instance.assets.forEach {
                    var name = it.unitName
                    if (name.length > 8) {
                        name = name.substring(0,8) + "..."
                    }
                    val sb = SpannableString(name)
                    if (it.assetId == this.assetId) {
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
                    this.assetId = asset?.assetId ?:0

                    amount.setText("")
                    secondAvailableSum.text = 0.0.convertToGroth().exchangeValueAsset(assetId)
                    currency.text = asset?.unitName ?: ""

                    presenter?.updateToken()

                    true
                })

                if (menu.menu is MenuBuilder) {
                    (menu.menu as MenuBuilder).setOptionalIconsVisible(true)
                }
                menu.show()
            }
        }

        txComment.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
               if (txComment.text.toString().isEmpty()) {
                   txComment.setTypeface(null, Typeface.ITALIC)
               }
                else {
                   txComment.setTypeface(null, Typeface.NORMAL)
               }
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateDropDownIcon(btnExpandCurrency, false)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        setFragmentResultListener("FragmentB_REQUEST_KEY") { key, bundle ->
            presenter?.state?.wasAddressSaved = true
        }

        if(App.isDarkMode) {
            backgroundView.setBackgroundResource(R.drawable.receive_bg_dark)
        }

        if(AssetManager.instance.assets.size <= 1) {
            btnExpandCurrency.visibility = View.GONE
        }

        currency.text = AssetManager.instance.getAsset(getAssetId())?.unitName
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
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

    override fun shareToken(receiveToken: String) {
        presenter?.state?.wasAddressSaved = true
        shareText(getString(R.string.common_share_title), receiveToken, activity)
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun showQR(receiveToken: String) {
        val address = presenter?.state?.address
        if (address!=null) {
            presenter?.state?.wasAddressSaved = true
            findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToQrDialogFragment(address,
                    0,
                    false,
                    receiveToken,
                    false,
                    presenter?.transaction == ReceivePresenter.TransactionTypeOptions.MAX_PRIVACY))
        }
    }

    override fun getComment(): String? = nameComment.text?.toString()

    override fun showSaveAddressDialog(nextStep: () -> Unit) {
        showAlert(
                title = getString(R.string.save_address),
                message = getString(R.string.receive_save_address_message),
                btnConfirmText = getString(R.string.save),
                btnCancelText = getString(R.string.dont_save),
                onCancel = {
                    presenter?.state?.address?.id?.let { AppManager.instance.wallet?.deleteAddress(it) }
                    nextStep()
                },
                onConfirm = {
                    presenter?.onSaveAddressPressed()
                    nextStep()
                }
        )
    }

    override fun showSaveChangesDialog(nextStep: () -> Unit) {
        showAlert(
                title = getString(R.string.save_changes),
                message = getString(R.string.receive_save_changes_message),
                btnConfirmText = getString(R.string.save),
                btnCancelText = getString(R.string.dont_save),
                onCancel = {
                    presenter?.state?.address?.id?.let { AppManager.instance.wallet?.deleteAddress(it) }
                    nextStep()
                },
                onConfirm = {
                    presenter?.onSaveAddressPressed()
                    nextStep()
                }
        )
    }

    override fun showShowToken(receiveToken: String) {
        val name = if(presenter?.transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                getString(R.string.regular_online_address)
            }
            else {
                getString(R.string.regular_address)
            }
        }
        else {
            getString(R.string.max_anonymity_address)
        }

        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToShowTokenFragment(receiveToken, true, true, name))
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnShareToken.setOnClickListener(null)

        amount.removeTextChangedListener(amountWatcher)
        amount.onFocusChangeListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }

    private fun getMaxPrivacyStringValue(hours: Long): String? {
        when (hours) {
            24L -> {
                return getString(R.string.h24)
            }
            36L -> {
                return getString(R.string.h36)
            }
            48L -> {
                return getString(R.string.h48)
            }
            60L -> {
                return getString(R.string.h60)
            }
            72L -> {
                return getString(R.string.h72)
            }
            else -> return null
        }
    }
}
