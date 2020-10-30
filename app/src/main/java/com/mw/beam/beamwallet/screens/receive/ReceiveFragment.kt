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

import android.R.attr.spacing
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.ScaleXSpan
import android.transition.TransitionManager
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.views.TagAdapter
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.fragment_receive.secondAvailableSum
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import org.jetbrains.anko.withAlpha


/**
 *  11/13/18.
 */
class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {
    private val copyTag = "ADDRESS"
    private var token_1 = ""
    private var token_2 = ""

    private val amountWatcher: com.mw.beam.beamwallet.core.watchers.TextWatcher = object : com.mw.beam.beamwallet.core.watchers.TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            if(getAmount() != null) {
                if (getAmount()!! > 0) {
                    secondAvailableSum.visibility = View.VISIBLE
                    secondAvailableSum.text = getAmount()!!.convertToCurrencyString()
                }
                else {
                    secondAvailableSum.visibility = View.GONE
                }
            }
            else {
                secondAvailableSum.visibility = View.GONE
            }
            presenter?.updateToken()
        }
    }


    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            presenter?.onBackPressed()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_receive
    override fun getToolbarTitle(): String? = getString(R.string.receive)

    override fun getAmountFromArguments(): Long {
        return ReceiveFragmentArgs.fromBundle(requireArguments()).amount
    }

    override fun getWalletAddressFromArguments(): WalletAddress? {
        return ReceiveFragmentArgs.fromBundle(requireArguments()).walletAddress
    }

    override fun getAmount(): Double? = amount.text?.toString()?.toDoubleOrNull()
    override fun setAmount(newAmount: Double) {
        amount.setText(newAmount.convertToBeamString())
        secondAvailableSum.text = newAmount.convertToCurrencyString()
        if (newAmount > 0) {
            secondAvailableSum.visibility = View.VISIBLE
        }
        else {
            secondAvailableSum.visibility = View.GONE
        }
    }
    override fun getTxComment(): String? {
        return txComment?.text?.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init() {

        amount.filters = arrayOf(AmountFilter())
        amountTitle.text = "${getString(R.string.amount).toUpperCase()} (${getString(R.string.optional).toLowerCase()})"

        if(App.isDarkMode) {
            addressGroup.setBackgroundColor(requireContext().getColor(R.color.colorPrimary_dark).withAlpha(95))
        }
        else {
            addressGroup.setBackgroundColor(requireContext().getColor(R.color.colorPrimary).withAlpha(95))
        }
}

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.received_color)
    }

    override fun updateTokens(walletAddress: WalletAddress) {
        if (tokenValue_1!=null) {
            tokenValue_1.text = walletAddress.token.trimAddress()
            token_1 = walletAddress.token

            tokenValue_2.text = walletAddress.offlineToken.trimAddress()
            token_2 = walletAddress.offlineToken
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions, expire: ReceivePresenter.TokenExpireOptions){
        comment.setText(walletAddress.label)

        tokenTitle_1.text = resources.getString(R.string.online_token).toUpperCase() + " (" + resources.getString(R.string.for_wallet).toLowerCase() + ")"
        tokenValue_1.text = walletAddress.token.trimAddress()
        token_1 = walletAddress.token

        val value = ScreenHelper.dpToPx(context, 15)

        if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            receiveDescription.text = resources.getString(R.string.receive_description)

            regularButton.setPaddingRelative(value,0,value,0)
            maxPrivacyButton.setPaddingRelative(0,0,0,0)

            regularButton.setTextColor(resources.getColor(R.color.accent, null))
            maxPrivacyButton.setTextColor(resources.getColor(android.R.color.white, null))

            regularButton.setBackgroundResource(R.drawable.accent_btn_background)
            maxPrivacyButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }
        else {
            receiveDescription.text = resources.getString(R.string.receive_notice_max_privacy)

            maxPrivacyButton.setPaddingRelative(value,0,value,0)
            regularButton.setPaddingRelative(0,0,0,0)

            maxPrivacyButton.setTextColor(resources.getColor(R.color.accent, null))
            regularButton.setTextColor(resources.getColor(android.R.color.white, null))

            maxPrivacyButton.setBackgroundResource(R.drawable.accent_btn_background)
            regularButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        if(expire == ReceivePresenter.TokenExpireOptions.ONETIME) {
            oneTimeButton.setPaddingRelative(value,0,value,0)
            permanentButton.setPaddingRelative(0,0,0,0)

            oneTimeButton.setTextColor(resources.getColor(R.color.accent, null))
            permanentButton.setTextColor(resources.getColor(android.R.color.white, null))

            oneTimeButton.setBackgroundResource(R.drawable.accent_btn_background)
            permanentButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }
        else {
            permanentButton.setPaddingRelative(value,0,value,0)
            oneTimeButton.setPaddingRelative(0,0,0,0)

            permanentButton.setTextColor(resources.getColor(R.color.accent, null))
            oneTimeButton.setTextColor(resources.getColor(android.R.color.white, null))

            permanentButton.setBackgroundResource(R.drawable.accent_btn_background)
            oneTimeButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR && expire == ReceivePresenter.TokenExpireOptions.ONETIME) {
            tokenGroup_2.visibility = View.GONE
            tokenGroup_3.visibility = View.VISIBLE
            tokenGroup_1.visibility = View.VISIBLE

            qrCodeButton_2.visibility = View.GONE
        }
        else if(transaction == ReceivePresenter.TransactionTypeOptions.MAX_PRIVACY && expire == ReceivePresenter.TokenExpireOptions.ONETIME) {
            tokenValue_2.text = walletAddress.offlineToken.trimAddress()
            token_2 = walletAddress.offlineToken

            tokenGroup_2.visibility = View.VISIBLE
            tokenGroup_3.visibility = View.VISIBLE
            tokenGroup_1.visibility = View.VISIBLE

            qrCodeButton_2.visibility = View.GONE

            tokenTitle_2.text = resources.getString(R.string.offline_token).toUpperCase()+ " (" + resources.getString(R.string.for_wallet).toLowerCase() + ")"
        }
        else if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR && expire == ReceivePresenter.TokenExpireOptions.PERMANENT) {
            tokenValue_2.text = walletAddress.walletID.trimAddress()
            token_2 = walletAddress.walletID

            qrCodeButton_2.visibility = View.VISIBLE

            tokenGroup_2.visibility = View.VISIBLE
            tokenGroup_3.visibility = View.GONE
            tokenGroup_1.visibility = View.VISIBLE

            tokenTitle_2.text = resources.getString(R.string.online_token).toUpperCase() + " (" + resources.getString(R.string.for_pool).toLowerCase() + ")"
        }
        else if(transaction == ReceivePresenter.TransactionTypeOptions.MAX_PRIVACY && expire == ReceivePresenter.TokenExpireOptions.PERMANENT) {
            tokenValue_2.text = walletAddress.offlineToken.trimAddress()
            token_2 = walletAddress.offlineToken

            qrCodeButton_2.visibility = View.GONE

            tokenGroup_1.visibility = View.GONE
            tokenGroup_2.visibility = View.VISIBLE
            tokenGroup_3.visibility = View.GONE

            tokenTitle_2.text = resources.getString(R.string.offline_token).toUpperCase() + " (" + resources.getString(R.string.for_wallet).toLowerCase() + ")"
        }

        if (expire == ReceivePresenter.TokenExpireOptions.PERMANENT) {
            editAddressCard.visibility = View.VISIBLE
        }
        else {
            editAddressCard.visibility = View.GONE
        }

        AppActivity.self.runOnUiThread {
            if(!AppManager.instance.isOwnNode()) {
                notAvailableLabel.text = getString(R.string.max_privacy_disabled_node)
                notAvailableLabel.setTypeface(null, android.graphics.Typeface.NORMAL)
                notAvailableLabel.visibility = View.VISIBLE

                maxPrivacyButton.isEnabled = false
                maxPrivacyButton.alpha = 0.2f

                tokenExpireTitle.visibility = View.VISIBLE
                permanentButton.visibility = View.VISIBLE
                oneTimeButton.visibility = View.VISIBLE
            }
            else if (transaction == ReceivePresenter.TransactionTypeOptions.MAX_PRIVACY) {
                notAvailableLabel.text = getString(R.string.max_privacy_text)
                notAvailableLabel.setTypeface(null, android.graphics.Typeface.ITALIC)
                notAvailableLabel.visibility = View.VISIBLE

                if(expire != ReceivePresenter.TokenExpireOptions.PERMANENT) {
                    presenter?.onPermanentPressed()
                }

                tokenExpireTitle.visibility = View.GONE
                permanentButton.visibility = View.GONE
                oneTimeButton.visibility = View.GONE
            }
            else {
                tokenExpireTitle.visibility = View.VISIBLE
                permanentButton.visibility = View.VISIBLE
                oneTimeButton.visibility = View.VISIBLE

                notAvailableLabel.visibility = View.GONE
            }
        }
    }

    override fun copyAddress(address: String) {
        copyToClipboard(address, copyTag)
        showSnackBar(getString(R.string.address_copied_to_clipboard))
    }

    override fun handleExpandAdvanced(expand: Boolean) {
        animateDropDownIcon(btnExpandAdvanced, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        advancedGroup.visibility = if (expand) View.VISIBLE else View.GONE
    }

    override fun handleExpandEditAddress(expand: Boolean) {
        animateDropDownIcon(btnExpandEditAddress, expand)
        TransitionManager.beginDelayedTransition(contentLayout)
        editAddressGroup.visibility = if (expand) View.VISIBLE else View.GONE
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 360f else 180f
        val angleTo = if (shouldExpand) 180f else 360f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }


    override fun addListeners() {
        btnShareToken.setOnClickListener {
            presenter?.onShareTokenPressed()
        }

        advancedContainer.setOnClickListener {
            presenter?.onAdvancedPressed()
        }

        editAddressContainer.setOnClickListener {
            presenter?.onEditAddressPressed()
        }

        oneTimeButton.setOnClickListener {
            presenter?.onOneTimePressed()
        }

        permanentButton.setOnClickListener {
            presenter?.onPermanentPressed()
        }

        regularButton.setOnClickListener {
            presenter?.onRegularPressed()
        }

        maxPrivacyButton.setOnClickListener {
            presenter?.onMaxPrivacyPressed()
        }

        amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                amount.hint = "0"
                showKeyboard()
            } else {
                amount.hint = ""
            }
        }

        amount.addTextChangedListener(amountWatcher)

        tagAction.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        tags.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        comment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter?.setAddressName(comment.text.toString())
            }
        })

        showTokenButton_1.setOnClickListener {
            presenter?.onTokenPressed(token_1)
        }

        showTokenButton_2.setOnClickListener {
            presenter?.onTokenPressed(token_2)
        }

        qrCodeButton_1.setOnClickListener {
            presenter?.onShowQrPressed(token_1)
        }

        qrCodeButton_2.setOnClickListener {
            presenter?.onShowQrPressed(token_2)
        }

//        tokenValue_1.setOnLongClickListener {
//            copyToClipboard(token_1, "")
//            showSnackBar(getString(R.string.address_copied_to_clipboard))
//            return@setOnLongClickListener true
//        }
//
//        tokenValue_2.setOnLongClickListener {
//            copyToClipboard(token_2, "")
//            showSnackBar(getString(R.string.address_copied_to_clipboard))
//            return@setOnLongClickListener true
//        }

        registerForContextMenu(tokenValue_1)
        registerForContextMenu(tokenValue_2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        // Step 1. Listen for fragment results
        setFragmentResultListener("FragmentB_REQUEST_KEY") { key, bundle ->
            presenter?.state?.wasAddressSaved = true
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
                    receiveToken))
        }
    }

    override fun getComment(): String? = comment.text?.toString()

    override fun setupTagAction(isEmptyTags: Boolean) {
        val resId = if (isEmptyTags) R.drawable.ic_add_tag else R.drawable.ic_edit_tag
        val drawable = ContextCompat.getDrawable(requireContext(), resId)
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
        BottomSheetDialog(requireContext(), R.style.common_bottom_sheet_style).apply {
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

    override fun showShareDialog(option1:String, option2:String) {
        BottomSheetDialog(requireContext(), R.style.common_bottom_sheet_style).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.share_bottom_sheet, null)
            setContentView(view)

            val shareView1 = view.findViewById<TextView>(R.id.shareView1)
            val shareView2 = view.findViewById<TextView>(R.id.shareView2)
            val btnBottomSheetClose = view.findViewById<ImageView>(R.id.btnBottomSheetClose)

            shareView1.text = option1
            shareView2.text = option2

            shareView1.setOnClickListener {
                dismiss()
                shareToken(token_1)
            }

            shareView2.setOnClickListener {
                dismiss()
                shareToken(token_2)
            }

            btnBottomSheetClose.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    override fun setTags(tags: List<Tag>) {
        if (tags.count() == 0) {
            this.tags.text = getString(R.string.none)
        }
        else{
            this.tags.text = tags.createSpannableString(requireContext())
        }
    }

    override fun showSaveAddressDialog(nextStep: () -> Unit) {
        showAlert(
                title = getString(R.string.save_address),
                message = getString(R.string.receive_save_address_message),
                btnConfirmText = getString(R.string.save),
                btnCancelText = getString(R.string.dont_save),
                onCancel = { nextStep() },
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
                onCancel = { nextStep() },
                onConfirm = {
                    presenter?.onSaveAddressPressed()
                    nextStep()
                }
        )
    }

    override fun showAddNewCategory() {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToEditCategoryFragment())
    }

    override fun showShowToken(receiveToken: String) {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToShowTokenFragment(receiveToken))
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnShareToken.setOnClickListener(null)
        advancedContainer.setOnClickListener(null)
        editAddressContainer.setOnClickListener(null)
        tagAction.setOnTouchListener(null)
        tokenValue_1.setOnClickListener(null)
        tokenValue_2.setOnClickListener(null)

        amount.removeTextChangedListener(amountWatcher)
        amount.onFocusChangeListener = null

        showTokenButton_1.setOnClickListener(null)
        showTokenButton_2.setOnClickListener(null)

        qrCodeButton_1.setOnClickListener(null)
        qrCodeButton_2.setOnClickListener(null)

        oneTimeButton.setOnClickListener(null)
        permanentButton.setOnClickListener(null)
        maxPrivacyButton.setOnClickListener(null)
        regularButton.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val copy = SpannableStringBuilder()
        copy.append(getString(R.string.copy))
        copy.setSpan(ForegroundColorSpan(Color.WHITE),
                0, copy.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        menu.add(0, v.id, 0, copy)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        if (item.itemId == tokenValue_1.id) {
            presenter?.state?.wasAddressSaved = true
            copyToClipboard(token_1, "")
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }  else if (item.itemId == tokenValue_2.id) {
            presenter?.state?.wasAddressSaved = true
            copyToClipboard(token_2, "")
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }


        return super.onContextItemSelected(item)
    }
}
