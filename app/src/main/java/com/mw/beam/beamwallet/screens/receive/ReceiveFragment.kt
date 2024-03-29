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
import android.view.Menu
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
import com.mw.beam.beamwallet.core.views.onSizeChange
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

import kotlinx.android.synthetic.main.fragment_receive.*
import org.jetbrains.anko.withAlpha

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
        activity?.runOnUiThread {
            sbbsAddress = walletAddress.id
            offlineAddress = walletAddress.tokenOffline
            maxPrivacyAddress = walletAddress.tokenMaxPrivacy

            if (transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
                addressHintLabel.visibility = View.GONE
                if (!AppManager.instance.isMaxPrivacyEnabled()) {
                    addressLabel.text = walletAddress.address.trimAddress()
                }
                else {
                    addressLabel.text = walletAddress.tokenOffline.trimAddress()
                }
            }
            else {
                addressHintLabel.visibility = View.GONE
                addressLabel.text = walletAddress.tokenMaxPrivacy.trimAddress()
            }
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions){
        AppActivity.self.runOnUiThread {

        nameComment.setText(walletAddress.label)
        txComment.setText(walletAddress.label)

        if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            addressHintLabel.visibility = View.GONE
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
            addressHintLabel.visibility = View.GONE
        }

            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                //  notAvailableLabel.text = getString(R.string.max_privacy_disabled_node)
                // notAvailableLabel.visibility = View.VISIBLE

                switcherView.alpha = 0.5f
                maxLabel.alpha = 0.5f
                switchView.isEnabled = false

                amount.alpha = 0.5f
                amount.isEnabled = false

                currencyLayout.alpha = 0.5f
                currencyLayout.isEnabled = false

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
//        presenter?.saveToken()
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
//                presenter?.saveToken()
            }
            else {
                presenter?.onRegularPressed()
//                presenter?.saveToken()
            }
        }

        amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
            if (hasFocus) {
                amount.isStateAccent = true
            }
            else {
                amount.isStateNormal = true
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

        if(AssetManager.instance.filteredAssets.size != 1) {
            currencyLayout.setOnClickListener {
                animateDropDownIcon(btnExpandCurrency, true)
                val menu = PopupMenu(requireContext(), currencyLayout, Gravity.END, R.attr.listPopupWindowStyle, R.style.popupOverflowMenu)
                menu.gravity = Gravity.END;

                menu.setOnDismissListener {
                    animateDropDownIcon(btnExpandCurrency, false)
                }
                var index = 0
                AssetManager.instance.filteredAssets.forEach {
                    var name = it.unitName
                    if (name.length > 8) {
                        name = name.substring(0,8) + "..."
                    }
                    val sb = SpannableString(name + " " + "(${it.assetId})")
                    if (it.assetId == this.assetId) {
                        sb.setSpan(StyleSpan(Typeface.BOLD), 0, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sb.setSpan(ForegroundColorSpan(requireContext().getColor(R.color.colorAccent)), 0, sb.length, 0)
                    }
                    else {
                        sb.setSpan(ForegroundColorSpan(Color.WHITE), 0, sb.length, 0)
                    }

                    sb.setSpan(ForegroundColorSpan(Color.WHITE.withAlpha(125)), name.length, sb.length, 0)

                    val item = menu.menu.add(Menu.NONE, index, Menu.NONE, sb)
                    item.setIcon(it.image)
                    index += 1
                }
                menu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    val id = item.itemId
                    val asset = AssetManager.instance.filteredAssets[id]
                    this.assetId = asset.assetId

                    amount.setText("")
                    secondAvailableSum.text = 0.0.convertToGroth().exchangeValueAsset(assetId)
                    currency.text = asset.unitName
                    currencyImageView.setImageResource(asset.image)

                    val sb = SpannableString(asset.unitName + " " + "(${asset.assetId})")
                    sb.setSpan(ForegroundColorSpan(Color.WHITE.withAlpha(125)), asset.unitName.length, sb.length, 0)
                    currency.text = sb

                    presenter?.updateToken()
//                    presenter?.saveToken()

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
//            presenter?.saveToken()
        }

        if(App.isDarkMode) {
            backgroundView.setBackgroundResource(R.drawable.receive_bg_dark)
        }

        if(AssetManager.instance.filteredAssets.size <= 1) {
            btnExpandCurrency.visibility = View.GONE
        }

        val asset = AssetManager.instance.getAsset(getAssetId())
        currency.text = asset?.unitName
        currencyImageView.setImageResource(asset?.image ?: R.drawable.asset0)

        val sb = SpannableString(asset?.unitName + " " + "(${asset?.assetId})")
        sb.setSpan(ForegroundColorSpan(Color.WHITE.withAlpha(125)), asset?.unitName?.length ?: 0, sb.length, 0)
        currency.text = sb

        currencyLayout.onSizeChange {
            val w = currencyLayout.width + ScreenHelper.dpToPx(requireContext(), 20)
            amount.setPaddingRelative(ScreenHelper.dpToPx(requireContext(), 10),0, w, 0)
        }
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
//        presenter?.saveToken()
        shareText(getString(R.string.common_share_title), receiveToken, activity)
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun showQR(receiveToken: String) {
        val address = presenter?.state?.address
        if (address!=null) {
            presenter?.state?.wasAddressSaved = true
//            presenter?.saveToken()
            findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToQrDialogFragment(address,
                    0,
                    false,
                    receiveToken,
                    false,
                    presenter?.transaction == ReceivePresenter.TransactionTypeOptions.MAX_PRIVACY))
        }
    }

    override fun getComment(): String? = txComment.text?.toString()

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
                getString(R.string.regular_address)
                // getString(R.string.regular_online_address)
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
