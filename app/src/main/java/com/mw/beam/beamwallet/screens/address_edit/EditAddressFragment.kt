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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import kotlinx.android.synthetic.main.fragment_edit_address.*
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.*
import com.mw.beam.beamwallet.base_screen.*
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.Date
import java.util.Calendar
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.*

/**
 *  3/5/19.
 */
class EditAddressFragment : BaseFragment<EditAddressPresenter>(), EditAddressContract.View {
    private lateinit var expireNowString: String
    private lateinit var activateString: String
    private var needSave = false
    private var isContact = false

    private val commentTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onChangeComment(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter?.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })

            if (expireList.selectedItemPosition == 1) {
                expireLabel.visibility = View.GONE
            }
            else{
                var dt = Date()
                val c = Calendar.getInstance()
                c.time = dt
                c.add(Calendar.DATE, 1)
                dt = c.time

                expireLabel.visibility = View.VISIBLE
                expireLabel.text = CalendarUtils.fromDate(dt)
            }
        }
    }

    override fun getToolbarTitle(): String? { return getString(R.string.edit_address) }
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

        if (address.isContact) {
            initToolbar(getString(R.string.edit_contact), hasBackArrow = true, hasStatus = true)
            expireLayout.visibility = View.GONE
        }
        else{
            if (address.duration == 0L) {
                expireLabel.visibility = View.GONE
            }
            else if (address.isExpired) {
                expireTitleLabel.text = getText(R.string.expired)
                expireList.visibility = View.GONE
                expireLine.visibility = View.GONE
                expireLabel.setTextColor(resources.getColor(R.color.common_text_color))
            }

            expireLabel.text = CalendarUtils.fromTimestamp(address.createTime + address.duration)
        }

        val strings = requireContext().resources.getTextArray(R.array.receive_expires_periods)
        val adapter = object: ArrayAdapter<CharSequence>(requireContext(), android.R.layout.simple_spinner_item,strings) {
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {

                val view = View.inflate(context,android.R.layout.simple_spinner_item,null)
                val textView = view.findViewById(android.R.id.text1) as TextView
                textView.text = strings[position]
                if(position == expireList.selectedItemPosition) {
                    textView.setTextColor(resources.getColor(R.color.colorAccent))
                }
                else{
                    val txtColor = if (App.isDarkMode) {
                        resources.getColor(R.color.common_text_dark_color_dark)
                    } else{
                        resources.getColor(R.color.common_text_dark_color)
                    }
                    textView.setTextColor(txtColor)
                }
                textView.setPadding(15,30,15,30)

                val bgColor = if (App.isDarkMode) {
                    resources.getColor(R.color.colorPrimary_dark)
                } else{
                    resources.getColor(R.color.colorPrimary)
                }
                view.setBackgroundColor(bgColor)

                return view
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as AppCompatTextView).setTextColor(resources.getColor(R.color.common_text_color))
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                view.setPadding(0,0,0,0)
                return view
            }
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        expireList.adapter = adapter
        expireList.setSelection(if (address.duration == 0L) 1 else 0)

        setHasOptionsMenu(true)
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
            menu?.findItem(R.id.expire)?.isVisible = true
        }
        else if (presenter?.state?.shouldExpireNow == true) {
            menu?.findItem(R.id.active)?.isVisible = true
            menu?.findItem(R.id.expire)?.isVisible = false
        }
        else if (address.isExpired) {
            menu?.findItem(R.id.expire)?.isVisible = false
            menu?.findItem(R.id.active)?.isVisible = true
        }
        else{
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
        expireList.onItemSelectedListener = expireListener

        btnSave.setOnClickListener {
            if (needSave) {
                presenter?.onSavePressed()
            }
            else{
                findNavController().popBackStack()
            }
        }

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        nameLabel.addTextChangedListener(commentTextWatcher)
    }


    override fun configExpireSpinnerTime(shouldExpireNow: Boolean) {

        if (shouldExpireNow) {
            expireTitleLabel.text = getText(R.string.expired)
            expireList.visibility = View.GONE
            expireLine.visibility = View.GONE
            expireLabel.setTextColor(resources.getColor(R.color.common_text_color))
            expireLabel.text = CalendarUtils.fromTimestamp(System.currentTimeMillis() / 1000)
            expireLabel.visibility = View.VISIBLE
        }
        else{
            var dt = Date()
            val c = Calendar.getInstance()
            c.time = dt
            c.add(Calendar.DATE, 1)
            dt = c.time

            expireTitleLabel.text = getText(R.string.expires)
            expireList.visibility = View.VISIBLE
            expireLine.visibility = View.VISIBLE
            expireLabel.setTextColor(resources.getColor(R.color.common_text_dark_color))
            expireLabel.text = CalendarUtils.fromDate(dt)
            expireLabel.visibility = View.VISIBLE

            expireList.setSelection(0)

            presenter?.onExpirePeriodChanged(ExpirePeriod.DAY)
        }

        activity?.invalidateOptionsMenu()
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
        expireList.onItemSelectedListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return EditAddressPresenter(this, EditAddressRepository(), EditAddressState())
    }
}

