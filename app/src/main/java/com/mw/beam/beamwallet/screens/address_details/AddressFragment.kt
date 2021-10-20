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
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import kotlinx.android.synthetic.main.fragment_address.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.ExchangeManager

/**
 *  3/4/19.
 */
class AddressFragment : BaseFragment<AddressPresenter>(), AddressContract.View {
    private var adapter: TransactionsAdapter? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_address
    override fun getToolbarTitle(): String? = getString(if (addressDetails?.isContact == true) R.string.contact_details else R.string.address_details)
    override fun getAddress(): WalletAddress = AddressFragmentArgs.fromBundle(requireArguments()).walletAddress
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    var addressId = ""
    var addressDetails: WalletAddress? = null
    var isContact = false

    override fun init(address: WalletAddress) {
        toolbarLayout.hasStatus = true

        addressDetails = address
        addressId = address.displayAddress ?: address.address

        configAddressDetails(address)

        initTransactionsList()

        setHasOptionsMenu(true)

        if(addressDetails?.isContact == true) {
            initToolbar(getString(R.string.contact_details), hasBackArrow = true, hasStatus = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.address_menu, menu)
        presenter?.onMenuCreate(menu)
    }

    override fun configMenuItems(menu: Menu?, address: WalletAddress) {
        if (isContact) {
            menu?.findItem(R.id.send)?.isVisible = true
            menu?.findItem(R.id.receive)?.isVisible = false
        }
        else if (!isContact && !address.isExpired) {
            menu?.findItem(R.id.send)?.isVisible = false
            menu?.findItem(R.id.receive)?.isVisible = true
        }
        else {
            menu?.findItem(R.id.receive)?.isVisible = false
            menu?.findItem(R.id.send)?.isVisible = false
        }

        menu?.findItem(R.id.delete)?.isVisible = false
        menu?.findItem(R.id.showQR)?.isVisible = address.isContact || !address.isExpired
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showQR -> presenter?.onShowQR()
            R.id.copy -> presenter?.onCopyAddress()
            R.id.edit -> presenter?.onEditAddress()
            R.id.delete -> presenter?.onDeleteAddress()
            R.id.send -> presenter?.onSendAddress()
            R.id.receive -> presenter?.onReceiveAddress()
        }

        return true
    }

    override fun copyToClipboard(content: String?, tag: String) {
        super.copyToClipboard(content, tag)

        showSnackBar(getString(R.string.address_copied_to_clipboard))
    }

    private fun initTransactionsList() {
        if (adapter == null) {
            adapter = TransactionsAdapter(requireContext(),null, mutableListOf(), TransactionsAdapter.Mode.SHORT) {
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
        ExchangeManager.instance.isPrivacyMode = isEnable
        adapter?.setPrivacyMode(isEnable)
    }

    private fun configAddressDetails(address: WalletAddress) {
        idLabel.text = address.displayAddress ?: address.address
        expirationLabel.text = if (address.duration == 0L) getString(R.string.never) else CalendarUtils.fromTimestamp(address.createTime + address.duration)
        nameLabel.text = address.label
        isContact = address.isContact

        if (nameLabel.text.isNullOrEmpty()) {
            nameLabel.text = getString(R.string.no_name)
        }

        val expirationVisibility = if (address.isContact) View.GONE else View.VISIBLE
       // expirationLayout.visibility = expirationVisibility

        if (address.identity.isNullOrEmpty() || address.identity == "0") {
            identityLayout.visibility = View.GONE
        }
        else {
            identityLayout.visibility = View.VISIBLE
            identityLabel.text = address.identity
        }
    }

    @SuppressLint("InflateParams")
    override fun showQR(walletAddress: WalletAddress) {
        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToQrDialogFragment(walletAddress, isOldDesign = true))
    }

    override fun showDeleteSnackBar(walletAddress: WalletAddress) {
        showSnackBar(getString(if (walletAddress.isContact) R.string.contact_deleted else R.string.address_deleted),
                onDismiss = { TrashManager.remove(walletAddress.id) },
                onUndo = { TrashManager.restore(walletAddress.id) })
    }

    override fun sendAddress(walletAddress: WalletAddress) {
        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToSendFragment(walletAddress.address))
    }

    override fun receiveAddress(walletAddress: WalletAddress) {
        var assetId:Int? = null
        val isToken = AppManager.instance.wallet?.isToken(walletAddress.address)
        if (isToken == true) {
            val params =
                AppManager.instance.wallet?.getTransactionParameters(walletAddress.address, false)

            if (params?.assetId != 0) {
                assetId = params?.assetId
            }
        }

        findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToReceiveFragment(0L, walletAddress, assetId ?: 0))
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
