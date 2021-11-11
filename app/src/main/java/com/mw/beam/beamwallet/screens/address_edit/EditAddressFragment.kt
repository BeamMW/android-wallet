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

package com.mw.beam.beamwallet.screens.address_edit

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.fragment_edit_address.*
import android.view.*
import androidx.core.widget.addTextChangedListener
import com.mw.beam.beamwallet.base_screen.*
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.*
import java.util.*

/**
 *  3/5/19.
 */
class EditAddressFragment : BaseFragment<EditAddressPresenter>(), EditAddressContract.View {
    private lateinit var expireNowString: String
    private lateinit var activateString: String
    private var needSave = false
    private var isContact = false

    private var canExtend = true
    private var canSave = false
    private var expireChanged = false
    private var canEditExpire = true
    private var isNeverExpired = false
    private val maxAddressDuration:Long = ((24 * 60 * 60) * 61)

    private val commentTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onChangeComment(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun getToolbarTitle(): String { return getString(R.string.edit_address) }
    override fun onControllerGetContentLayoutId() = R.layout.fragment_edit_address
    override fun getAddress(): WalletAddress = EditAddressFragmentArgs.fromBundle(requireArguments()).walletAddress
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    override fun init(address: WalletAddress) {
        isContact = address.isContact

        toolbarLayout.hasStatus = true

        expireNowString = getString(R.string.expire_address_now)
        activateString = getString(R.string.active_address)

        nameLabel.setText(address.label)

        addressLabel.text = address.displayAddress ?: address.address

        if (address.isContact) {
            initToolbar(getString(R.string.edit_contact), hasBackArrow = true, hasStatus = true)
            expireLayout.visibility = View.GONE
            btnSave.isEnabled = canSave
        }
        else{
            setExpireStatus()
        }

        nameLabel.addTextChangedListener {
            canSave = true
            btnSave.isEnabled = true
        }

        if (!AppManager.instance.hasActiveTransactionsAddress(presenter?.state?.address?.id ?: "")
            || !AppManager.instance.hasActiveTransactionsAddress(presenter?.state?.address?.address ?: "")) {
            setHasOptionsMenu(true)
        }
    }

    private fun setExpireStatus() {
        val address = presenter!!.state.address!!
        val state = presenter!!.state

        if (address.isExpired || state.shouldExpireNow) {
            extendButton.visibility = View.GONE
            expireLabel.text = getText(R.string.expired)
            expireButton.text = getString(R.string.activate)
        }
        else {
            if (address.duration == 0L) {
                extendButton.visibility = View.GONE
                expireLabel.text =  getString(R.string.address_never_expired)
            }
            else {
                expireLabel.text = CalendarUtils.fromTimestamp(address.createTime + address.duration)
            }
            expireButton.text = getString(R.string.expire_address_now)
        }

        if (!canExtend) {
            extendButton.alpha = 0.5f
        }
        else {
            extendButton.alpha = 1f
        }

        btnSave.isEnabled = canSave

        if (AppManager.instance.hasActiveTransactionsAddress(state.address?.id ?: "")
            || AppManager.instance.hasActiveTransactionsAddress(state.address?.address ?: "")) {
            setHasOptionsMenu(false)
            expireButton.isEnabled = false
            expireButton.alpha = 0.5f
            expireHintLabel.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.address_edit_menu, menu)
        presenter?.onMenuCreate(menu)
    }

    override fun configMenuItems(menu: Menu?, address: WalletAddress) {
        if (address.isContact) {
            menu?.findItem(R.id.delete)?.title = getString(R.string.delete_contact)
            menu?.findItem(R.id.expire)?.isVisible = false
            menu?.findItem(R.id.active)?.isVisible = false
        }
        else if (presenter?.state?.shouldActivateNow == true) {
            menu?.findItem(R.id.active)?.isVisible = false
            menu?.findItem(R.id.expire)?.isVisible = false
        }
        else if (presenter?.state?.shouldExpireNow == true) {
            menu?.findItem(R.id.active)?.isVisible = false
            menu?.findItem(R.id.expire)?.isVisible = false
        }
        else if (address.isExpired) {
            menu?.findItem(R.id.expire)?.isVisible = false
            menu?.findItem(R.id.active)?.isVisible = false
        }
        else{
            menu?.findItem(R.id.expire)?.isVisible = false
            menu?.findItem(R.id.active)?.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.active -> presenter?.onSwitchCheckedChange(false)
            R.id.expire -> presenter?.onSwitchCheckedChange(true)
            R.id.delete -> presenter?.onDeleteAddress()
        }

        return true
    }

    override fun showDeleteSnackBar(walletAddress: WalletAddress) {
        showSnackBar(getString(if (walletAddress.isContact) R.string.contact_deleted else R.string.address_deleted),
                onDismiss = { TrashManager.remove(walletAddress.id) },
                onUndo = { TrashManager.restore(walletAddress.id) })
    }

    override fun showDeleteAddressDialog(transactionAlert:Boolean) {
        if (transactionAlert) {
            context?.let {
                val view = LayoutInflater.from(it).inflate(R.layout.dialog_delete_address, null)

                if(isContact) {
                    view.clearDialogTitle.text = getString(R.string.delete_contact)
                    view.deleteAllTransactionsTitle.text = getString(R.string.delete_all_transactions_related_to_this_contact)
                }

                val dialog = AlertDialog.Builder(it).setView(view).show()

                view.btnConfirm.setOnClickListener {
                    presenter?.onConfirmDeleteAddress(view.deleteAllTransactionsCheckbox.isChecked)
                    dialog.dismiss()
                }

                view.btnCancel.setOnClickListener { dialog.dismiss() }

                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        else{
            val msgText = when {
                isContact -> getString(R.string.delete_contact_text)
                else -> getString(R.string.delete_address_text)
            }

            val titleText = when {
                isContact -> getString(R.string.delete_contact)
                else -> getString(R.string.delete_address)
            }

            showAlert(msgText,getString(R.string.delete),{
                presenter?.onConfirmDeleteAddress(false)
            },titleText,getString(R.string.cancel))
        }
    }


    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        nameLabel.addTextChangedListener(commentTextWatcher)

        expireButton.setOnClickListener {
            if (presenter?.state?.shouldActivateNow == true) {
                presenter?.state?.shouldActivateNow = false
                presenter?.state?.shouldExpireNow = true
            }
            else if (presenter?.state?.shouldExpireNow == true || presenter?.state?.address?.isExpired == true) {
                presenter?.state?.shouldActivateNow = true
                presenter?.state?.shouldExpireNow = false

                val c = Calendar.getInstance()
                c.time = Date()
                c.add(Calendar.DATE, 61)

                presenter?.state?.address?.isExpired = false
                presenter?.state?.address?.createTime = (Calendar.getInstance().timeInMillis/1000)
                presenter?.state?.address?.duration = maxAddressDuration
            }
            else {
                presenter?.state?.shouldActivateNow = false
                presenter?.state?.shouldExpireNow = true
            }
            canSave = true

            setExpireStatus()
        }

        extendButton.setOnClickListener {
            canExtend = false
            canSave = true

            presenter?.state?.shouldExtend = true
            presenter?.state?.shouldActivateNow = true
            presenter?.state?.shouldExpireNow = false

            val c = Calendar.getInstance()
            c.time = Date()
            c.add(Calendar.DATE, 61)

            presenter?.state?.address?.isExpired = false
            presenter?.state?.address?.createTime = (Calendar.getInstance().timeInMillis/1000)
            presenter?.state?.address?.duration = maxAddressDuration

            setExpireStatus()
        }
    }


    override fun configExpireSpinnerTime(shouldExpireNow: Boolean) {

    }

    override fun configSaveButton(shouldEnable: Boolean) {
        needSave = shouldEnable
    }

    override fun finishScreen() {
        findNavController().popBackStack()
    }

    override fun onAddressDeleted() {
        val isBack = findNavController().popBackStack(R.id.addressesFragment, false)
        if (!isBack) {
            findNavController().popBackStack(R.id.categoryFragment, false)
        }
    }

    override fun clearListeners() {
        btnSave.setOnClickListener(null)
        btnCancel.setOnClickListener(null)
        nameLabel.removeTextChangedListener(commentTextWatcher)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return EditAddressPresenter(this, EditAddressRepository(), EditAddressState())
    }
}

