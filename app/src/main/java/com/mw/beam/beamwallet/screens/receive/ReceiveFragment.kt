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
import android.transition.TransitionManager
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
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.createSpannableString
import com.mw.beam.beamwallet.core.views.TagAdapter
import com.mw.beam.beamwallet.core.watchers.AmountFilter
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressCallback
import com.mw.beam.beamwallet.screens.change_address.ChangeAddressFragment
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.receive_expire_spinner_item.view.*
import android.text.Editable
import android.text.TextWatcher



/**
 *  11/13/18.
 */
class ReceiveFragment : BaseFragment<ReceivePresenter>(), ReceiveContract.View {
    private val copyTag = "ADDRESS"

    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter?.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            presenter?.onBackPressed()
        }
    }

    private val changeAddressCallback = object : ChangeAddressCallback {
        override fun onChangeAddress(walletAddress: WalletAddress) {
            presenter?.onAddressChanged(walletAddress)
            ChangeAddressFragment.callback = null
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
    override fun setAmount(newAmount: Double) = amount.setText(newAmount.convertToBeamString())
    override fun getTxComment(): String? {
        return txComment?.text?.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun init() {

        val strings = context!!.resources.getTextArray(R.array.receive_expires_periods)
        val adapter = object: ArrayAdapter<CharSequence>(context!!, R.layout.receive_expire_spinner_item,strings) {
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {

                val view = View.inflate(context,R.layout.receive_expire_spinner_item,null)
                val textView = view.findViewById(R.id.expireLabelPickerID) as TextView
                textView.text = strings[position]
                if(position == expiresOnSpinner.selectedItemPosition) {
                    textView.setTextColor(resources.getColor(R.color.colorAccent))
                }
                return view

            }
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        expiresOnSpinner.setSelection(0)
        expiresOnSpinner.adapter = adapter

        amount.filters = arrayOf(AmountFilter())
        amountTitle.text = "${getString(R.string.request_an_amount).toUpperCase()} (${getString(R.string.optional).toLowerCase()})"

        //crash test
//        val array = mutableListOf<String>()
//        val string = array[100]
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.received_color)
    }

    override fun initAddress(isGenerateAddress: Boolean, walletAddress: WalletAddress) {
        tokenTitle.text = if (isGenerateAddress) "${getString(R.string.address).toUpperCase()} (${getString(R.string.auto_generated).toLowerCase()})" else getString(R.string.address).toUpperCase()

        expiresOnSpinner.setSelection(if (walletAddress.duration == 0L) ExpirePeriod.NEVER.ordinal else ExpirePeriod.DAY.ordinal)

        comment.setText(walletAddress.label)

        token.text = walletAddress.walletID
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
        expiresOnSpinner.onItemSelectedListener = expireListener

        advancedContainer.setOnClickListener {
            presenter?.onAdvancedPressed()
        }

        editAddressContainer.setOnClickListener {
            presenter?.onEditAddressPressed()
        }

        btnChangeAddress.setOnClickListener {
            presenter?.onChangeAddressPressed()
        }

        amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                amount.hint = "0"
                showKeyboard()
            } else {
                amount.hint = ""
            }
        }

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
        shareText(getString(R.string.common_share_title), receiveToken)
    }

    override fun showChangeAddressFragment(generatedAddress: WalletAddress?) {
        ChangeAddressFragment.callback = changeAddressCallback
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToChangeAddressFragment(generatedAddress = generatedAddress))
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    @SuppressLint("InflateParams")
    override fun showQR(walletAddress: WalletAddress, amount: Long?, isAutogenerated: Boolean) {
        findNavController().navigate(ReceiveFragmentDirections.actionReceiveFragmentToQrDialogFragment(walletAddress, amount ?: 0, isAutogenerated))
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

    override fun close() {
        findNavController().popBackStack()
    }

    override fun clearListeners() {
        btnShareToken.setOnClickListener(null)
        btnShowQR.setOnClickListener(null)
        btnChangeAddress.setOnClickListener(null)
        advancedContainer.setOnClickListener(null)
        editAddressContainer.setOnClickListener(null)
        token.setOnTouchListener(null)
        tagAction.setOnTouchListener(null)

        amount.onFocusChangeListener = null
        expiresOnSpinner.onItemSelectedListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ReceivePresenter(this, ReceiveRepository(), ReceiveState())
    }
}
