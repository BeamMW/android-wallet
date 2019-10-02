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

package com.mw.beam.beamwallet.screens.address_details

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.helpers.createSpannableString
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import kotlinx.android.synthetic.main.fragment_address.*

/**
 *  3/4/19.
 */
class AddressFragment : BaseFragment<AddressPresenter>(), AddressContract.View {
    private var adapter: TransactionsAdapter? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_address
    override fun getToolbarTitle(): String? = null
    override fun getAddress(): WalletAddress = AddressFragmentArgs.fromBundle(arguments!!).walletAddress
    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun init(address: WalletAddress) {
        toolbarLayout.hasStatus = true

        (activity as BaseActivity<*>).supportActionBar?.title = getString(if (address.isContact) R.string.contact_details else R.string.address_details)

        configAddressDetails(address)

        initTransactionsList()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.address_menu, menu)
        presenter?.onMenuCreate(menu)
    }

    override fun configMenuItems(menu: Menu?, address: WalletAddress) {
        menu?.findItem(R.id.delete)?.isVisible = false
        menu?.findItem(R.id.showQR)?.isVisible = address.isContact || !address.isExpired
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showQR -> presenter?.onShowQR()
            R.id.copy -> presenter?.onCopyAddress()
            R.id.edit -> presenter?.onEditAddress()
            R.id.delete -> presenter?.onDeleteAddress()
        }

        return true
    }

    override fun copyToClipboard(content: String?, tag: String) {
        super.copyToClipboard(content, tag)

        showSnackBar(getString(R.string.address_copied_to_clipboard))
    }

    private fun initTransactionsList() {
        if (adapter == null) {
            adapter = TransactionsAdapter(context!!,null, mutableListOf(), true) {
                presenter?.onTransactionPressed(it)
            }
            adapter?.reverseColors = true
            transactionsList.layoutManager = LinearLayoutManager(context)
            transactionsList.adapter = adapter
        }
        else if (transactionsList.adapter == null) {
            transactionsList.layoutManager = LinearLayoutManager(context)
            transactionsList.adapter = adapter
        }
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        adapter?.setPrivacyMode(isEnable)
    }

    private fun configAddressDetails(address: WalletAddress) {
        idLabel.text = address.walletID
        expirationLabel.text = if (address.duration == 0L) getString(R.string.never) else CalendarUtils.fromTimestamp(address.createTime + address.duration)
        nameLabel.text = address.label

        if (nameLabel.text.isNullOrEmpty()) {
            nameLabel.text = getString(R.string.no_name)
        }

        val expirationVisibility = if (address.isContact) View.GONE else View.VISIBLE
        expirationLayout.visibility = expirationVisibility
    }

    override fun configureTags(findTag: List<Tag>) {
        val categoryVisibility = if (findTag.isEmpty()) View.GONE else View.VISIBLE
        tagsLayout.visibility = categoryVisibility
        tagsLabel.text = findTag.createSpannableString(context!!)
    }

    @SuppressLint("InflateParams")
    override fun showQR(walletAddress: WalletAddress) {
        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToQrDialogFragment(walletAddress))
    }

    override fun showDeleteSnackBar(walletAddress: WalletAddress) {
        showSnackBar(getString(if (walletAddress.isContact) R.string.contact_deleted else R.string.address_deleted),
                onDismiss = { TrashManager.remove(walletAddress.walletID) },
                onUndo = { TrashManager.restore(walletAddress.walletID) })
    }

    override fun showDeleteAddressDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_delete_address, null)
            val dialog = AlertDialog.Builder(it).setView(view).show()

            view.btnConfirm.setOnClickListener {
                presenter?.onConfirmDeleteAddress(view.deleteAllTransactionsCheckbox.isChecked)
                dialog.dismiss()
            }

            view.btnCancel.setOnClickListener { dialog.dismiss() }

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        transactionsLayout.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        transactionsList.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

        if (transactions.isNotEmpty()) {
            adapter?.data = transactions
            adapter?.notifyDataSetChanged()
        }
    }

    override fun showTransactionDetails(txDescription: TxDescription) {
        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToTransactionDetailsFragment(txDescription.id))
    }

    override fun showEditAddressScreen(address: WalletAddress) {
        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToEditAddressFragment(address))
    }

    override fun finishScreen() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AddressPresenter(this, AddressRepository(), AddressState())
    }
}
