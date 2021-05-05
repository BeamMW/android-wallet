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
import android.os.Bundle
import android.text.*
import android.transition.TransitionManager
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup.OnPositionChangedListener
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_receive.*
import org.jetbrains.anko.withAlpha


class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {

    private val copyTag = "ADDRESS"
    private var sbbsAddress = ""
    private var offlineAddress = ""
    private var maxPrivacyAddress = ""

    private val amountWatcher: com.mw.beam.beamwallet.core.watchers.TextWatcher = object : com.mw.beam.beamwallet.core.watchers.TextWatcher {
        override fun afterTextChanged(token: Editable?) {
            val amount = getAmount() ?: 0.0
            secondAvailableSum.text = amount.convertToCurrencyString()
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

    override fun setAmount(newAmount: Double) {
        amount.setText(newAmount.convertToBeamString())
        secondAvailableSum.text = newAmount.convertToCurrencyString()
    }

    override fun getTxComment(): String? {
        return txComment?.text?.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init() {

        secondAvailableSum.text = (getAmount() ?: 0.0).convertToCurrencyString()

        amount.filters = arrayOf(AmountFilter())
        amountTitle.text = "${getString(R.string.requested_amount).toUpperCase()} (${getString(R.string.optional).toLowerCase()})"
    }


    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.received_color)
    }

    override fun updateTokens(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions) {
        sbbsAddress = walletAddress.id
        offlineAddress = walletAddress.tokenOffline
        maxPrivacyAddress = walletAddress.tokenMaxPrivacy

        if (transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            addressLabel.text = walletAddress.tokenOffline.trimAddress()
        }
        else {
            addressLabel.text = walletAddress.tokenMaxPrivacy.trimAddress()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions){
        nameComment.setText(walletAddress.label)

        val value = ScreenHelper.dpToPx(context, 15)

        if(transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            receiveDescription.text = resources.getString(R.string.receive_description)

//            regularButton.setPaddingRelative(value,0,value,0)
//            maxPrivacyButton.setPaddingRelative(0,0,0,0)
//
//            regularButton.setTextColor(resources.getColor(R.color.accent, null))
//            maxPrivacyButton.setTextColor(resources.getColor(android.R.color.white, null))
//
//            regularButton.setBackgroundResource(R.drawable.accent_btn_background)
//            maxPrivacyButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }
        else {
            receiveDescription.text = resources.getString(R.string.receive_notice_max_privacy)

//            maxPrivacyButton.setPaddingRelative(value,0,value,0)
//            regularButton.setPaddingRelative(0,0,0,0)
//
//            maxPrivacyButton.setTextColor(resources.getColor(R.color.accent, null))
//            regularButton.setTextColor(resources.getColor(android.R.color.white, null))
//
//            maxPrivacyButton.setBackgroundResource(R.drawable.accent_btn_background)
//            regularButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        AppActivity.self.runOnUiThread {
            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                notAvailableLabel.text = getString(R.string.max_privacy_disabled_node)
                notAvailableLabel.visibility = View.VISIBLE

                buttonGroupDraggable.isEnabled = false
                maxPrivacyButton.isEnabled = false
                maxPrivacyButton.textColor = resources.getColor(R.color.white_01, null)
            }
        }

        if (transaction == ReceivePresenter.TransactionTypeOptions.REGULAR) {
            addressLabel.text = walletAddress.tokenOffline.trimAddress()
        }
        else {
            addressLabel.text = walletAddress.tokenMaxPrivacy.trimAddress()
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

    override fun addListeners() {
        btnShareToken.setOnClickListener {
            presenter?.onShareTokenPressed()
        }

        txCommentContainer.setOnClickListener {
            presenter?.onCommentPressed()
        }

        amountContainer.setOnClickListener {
            presenter?.onAmountPressed()
        }

        buttonGroupDraggable.onPositionChangedListener = OnPositionChangedListener {
           if (it == 0) {
               presenter?.onRegularPressed()
           }
            else {
               presenter?.onMaxPrivacyPressed()
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        setFragmentResultListener("FragmentB_REQUEST_KEY") { key, bundle ->
            presenter?.state?.wasAddressSaved = true
        }

        if(App.isDarkMode) {
            nameLayout.setBackgroundColor(requireContext().getColor(R.color.colorPrimary_dark).withAlpha(95))
        }
        else{
            nameLayout.setBackgroundColor(requireContext().getColor(R.color.colorPrimary).withAlpha(95))
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
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToShowTokenFragment(receiveToken))
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnShareToken.setOnClickListener(null)

        amount.removeTextChangedListener(amountWatcher)
        amount.onFocusChangeListener = null

//        maxPrivacyButton.setOnClickListener(null)
//        regularButton.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }
}
