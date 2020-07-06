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
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressFragment
import kotlinx.android.synthetic.main.fragment_receive.*
import org.jetbrains.anko.withAlpha


/**
 *  11/13/18.
 */
class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {
    private val copyTag = "ADDRESS"

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
        return ReceiveFragmentArgs.fromBundle(arguments!!).amount
    }

    override fun getWalletAddressFromArguments(): WalletAddress? {
        return ReceiveFragmentArgs.fromBundle(arguments!!).walletAddress
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
        amountTitle.text = "${getString(R.string.request_an_amount).toUpperCase()} (${getString(R.string.optional).toLowerCase()})"

        if(App.isDarkMode) {
            addressGroup.setBackgroundColor(context!!.getColor(R.color.colorPrimary_dark).withAlpha(95))
        }
        else {
            addressGroup.setBackgroundColor(context!!.getColor(R.color.colorPrimary).withAlpha(95))
        }
}

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.received_color)
    }

    override fun initAddress(walletAddress: WalletAddress, expire: ReceivePresenter.ExpireOptions, receive: ReceivePresenter.ReceiveOptions){
        comment.setText(walletAddress.label)

        val value = ScreenHelper.dpToPx(context, 15)

        if(receive == ReceivePresenter.ReceiveOptions.POOL) {
            token.text = walletAddress.walletID.trimAddress()

            poolButton.setPaddingRelative(value,0,value,0)
            walletButton.setPaddingRelative(0,0,0,0)

            poolButton.setTextColor(resources.getColor(R.color.accent, null))
            walletButton.setTextColor(resources.getColor(android.R.color.white, null))

            poolButton.setBackgroundResource(R.drawable.accent_btn_background)
            walletButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }
        else {
            token.text = walletAddress.token.trimAddress()

            walletButton.setPaddingRelative(value,0,value,0)
            poolButton.setPaddingRelative(0,0,0,0)

            walletButton.setTextColor(resources.getColor(R.color.accent, null))
            poolButton.setTextColor(resources.getColor(android.R.color.white, null))

            walletButton.setBackgroundResource(R.drawable.accent_btn_background)
            poolButton.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        if(expire == ReceivePresenter.ExpireOptions.ONETIME) {
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
        btnShareToken.setOnClickListener { presenter?.onShareTokenPressed() }
        btnShowQR.setOnClickListener { presenter?.onShowQrPressed() }

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

        walletButton.setOnClickListener {
            presenter?.onWalletPressed()
        }

        poolButton.setOnClickListener {
            presenter?.onPoolPressed()
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

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                presenter?.onAddressLongPressed()
            }
        })

        tagAction.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        tags.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        token.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        comment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter?.setAddressName(comment.text.toString())
            }
        })

        showTokenButton.setOnClickListener {
            presenter?.onTokenPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
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
        shareText(getString(R.string.common_share_title), receiveToken, activity)
    }


    override fun getLifecycleOwner(): LifecycleOwner = this

    @SuppressLint("InflateParams")
    override fun showQR(walletAddress: WalletAddress, amount: Long?) {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToQrDialogFragment(walletAddress,
                amount ?: 0,
                presenter?.receive == ReceivePresenter.ReceiveOptions.WALLET))
    }

    override fun getComment(): String? = comment.text?.toString()

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

    override fun setTags(tags: List<Tag>) {
        if (tags.count() == 0) {
            this.tags.text = getString(R.string.none)
        }
        else{
            this.tags.text = tags.createSpannableString(context!!)
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
        btnShowQR.setOnClickListener(null)
        advancedContainer.setOnClickListener(null)
        editAddressContainer.setOnClickListener(null)
        token.setOnTouchListener(null)
        tagAction.setOnTouchListener(null)
        amount.removeTextChangedListener(amountWatcher)
        showTokenButton.setOnClickListener(null)
        amount.onFocusChangeListener = null

        oneTimeButton.setOnClickListener(null)
        permanentButton.setOnClickListener(null)
        walletButton.setOnClickListener(null)
        poolButton.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }
}
