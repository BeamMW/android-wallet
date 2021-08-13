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

package com.mw.beam.beamwallet.screens.addresses

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import kotlinx.android.synthetic.main.fragment_addresses.*
import kotlinx.android.synthetic.main.fragment_addresses.toolbarLayout
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_addresses.itemsswipetorefresh
import kotlinx.android.synthetic.main.fragment_addresses.pager
import kotlinx.android.synthetic.main.fragment_addresses.tabLayout
import kotlinx.android.synthetic.main.toolbar.*
import com.mw.beam.beamwallet.core.App

/**
 *  2/28/19.
 */
class AddressesFragment : BaseFragment<AddressesPresenter>(), AddressesContract.View {
    enum class Mode {
        NONE, EDIT
    }

    private val copyTag = "ADDRESS"

    private lateinit var pagerAdapter: AddressesPagerAdapter

    override fun onControllerGetContentLayoutId() = R.layout.fragment_addresses
    override fun getToolbarTitle(): String? = getString(R.string.addresses)

    private var selectedAddresses = mutableListOf<String>()
    private var mode = Mode.NONE
    private var menuPosition = 0

    private var removedAddresses = mutableListOf<String>()

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mode == Mode.NONE) {
                showWalletFragment()
            } else {
                cancelSelectedAddresses()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        itemsswipetorefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        itemsswipetorefresh.setOnRefreshListener {
            AppManager.instance.reload()
            android.os.Handler().postDelayed({
                if (itemsswipetorefresh!=null) {
                    itemsswipetorefresh.isRefreshing = false
                }
            }, 1000)
        }
    }

    override fun init() {

        val context = context ?: return

        setHasOptionsMenu(true)
        setMenuVisibility(false)

        pagerAdapter = AddressesPagerAdapter(context,
                object : AddressesAdapter.OnItemClickListener {
                    override fun onItemClick(item: WalletAddress) {
                        if (mode == Mode.NONE) {
                            presenter?.onAddressPressed(item)
                        } else {
                            if (selectedAddresses.contains(item.id)) {
                                selectedAddresses.remove(item.id)
                            } else {
                                selectedAddresses.add(item.id)
                            }

                            presenter?.isAllSelected = selectedAddresses.count() == presenter?.state?.filteredAddresses(pager.currentItem)?.count()

                            onSelectedAddressesChanged()
                        }
                    }
                },
                object : AddressesAdapter.OnLongClickListener {
                    override fun onLongClick(item: WalletAddress) {
                        if (mode == Mode.NONE) {

                            presenter?.onModeChanged(Mode.EDIT)

                            selectedAddresses.add(item.id)

                            pagerAdapter.changeSelectedItems(selectedAddresses, true, item.id)

                            pagerAdapter.reloadData(mode)

                            presenter?.isAllSelected = selectedAddresses.count() == presenter?.state?.filteredAddresses(pager.currentItem)?.count()

                            onSelectedAddressesChanged()
                        }
                    }
                })

        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                menuPosition = position
                pagerAdapter.changeSelectedItems(selectedAddresses, false, null)
                if (mode == Mode.NONE) {
                    setMenuVisibility(position == Tab.CONTACTS.value)
                }
            }
        })

        tabLayout.setupWithViewPager(pager)

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
        presenter?.onModeChanged(mode)
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

    private fun cancelSelectedAddresses() {
        presenter?.isAllSelected = false

        val toolbarLayout = toolbarLayout
        toolbarLayout.centerTitle = true
        toolbarLayout.toolbar.title = null
        toolbarLayout.toolbar.setNavigationIcon(R.drawable.ic_menu)

        presenter?.onModeChanged(Mode.NONE)

        selectedAddresses.clear()

        pagerAdapter.changeSelectedItems(selectedAddresses, false, null)

        pagerAdapter.reloadData(mode)

        activity?.invalidateOptionsMenu()

        setMenuVisibility(menuPosition == Tab.CONTACTS.value)
    }

    private fun onSelectedAddressesChanged() {
        val toolbarLayout = toolbarLayout
        toolbarLayout.centerTitle = false
        toolbarLayout.toolbar.title = selectedAddresses.count().toString() + " " + getString(R.string.selected).toLowerCase()
        toolbarLayout.toolbar.setNavigationIcon(R.drawable.ic_btn_cancel)
        toolbarLayout.toolbar.setNavigationOnClickListener {
            if (mode == Mode.NONE) {
                (activity as? AppActivity)?.openMenu()
            } else {
                cancelSelectedAddresses()
            }
        }

        if (selectedAddresses.count() == 0) {
            cancelSelectedAddresses()
        } else {
            setMenuVisibility(true)
            activity?.invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.addresses_menu, menu)

        if (mode == Mode.NONE) {
            menu.findItem(R.id.copy).isVisible = false
            menu.findItem(R.id.delete).isVisible = false
            menu.findItem(R.id.edit).isVisible = false
            menu.findItem(R.id.add).isVisible = true
            menu.findItem(R.id.all).isVisible = false
        }
        else {
            if (selectedAddresses.count() == 1) {
                menu.findItem(R.id.copy).isVisible = true
                menu.findItem(R.id.delete).isVisible = true
                menu.findItem(R.id.edit).isVisible = true
                menu.findItem(R.id.add).isVisible = false
            } else {
                menu.findItem(R.id.copy).isVisible = false
                menu.findItem(R.id.edit).isVisible = false
                menu.findItem(R.id.add).isVisible = false
                menu.findItem(R.id.delete).isVisible = true
            }
            menu.findItem(R.id.all).isVisible = true
            if  (!presenter!!.isAllSelected) {
                menu.findItem(R.id.all).icon = resources.getDrawable(R.drawable.ic_checkbox_empty_copy)
            }
            else{
                menu.findItem(R.id.all).icon = resources.getDrawable(R.drawable.ic_checkbox_fill_copy)
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {
            presenter?.onAddContactPressed()
        } else if (item.itemId == R.id.edit) {
            presenter?.onEditAddressPressed()
        } else if (item.itemId == R.id.copy) {
            presenter?.onCopyAddressPressed()
        } else if (item.itemId == R.id.delete) {
            presenter?.onDeleteAddressesPressed()
        } else if (item.itemId == R.id.all) {
            presenter?.onSelectAll()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun didSelectAllAddresses(addresses: List<WalletAddress>) {
        val filtered = when (pager.currentItem) {
            0 -> {
                addresses.filter { !it.isExpired && !it.isContact }
            }
            1 -> {
                addresses.filter { it.isExpired && !it.isContact }
            }
            else -> {
                addresses.filter { it.isContact }
            }
        }


        selectedAddresses.clear()
        filtered.forEach {
            selectedAddresses.add(it.id)
        }
        pagerAdapter.changeSelectedItems(selectedAddresses, false, null)
        pagerAdapter.reloadData(mode)
        onSelectedAddressesChanged()
        activity?.invalidateOptionsMenu()
    }

    override fun didUnSelectAllAddresses() {
        presenter?.isAllSelected = false
        selectedAddresses.clear()
        pagerAdapter.changeSelectedItems(selectedAddresses, false, null)
        pagerAdapter.reloadData(mode)
        onSelectedAddressesChanged()
    }

    override fun navigateToAddContactScreen() {
        findNavController().navigate(AddressesFragmentDirections.actionAddressesFragmentToAddContactFragment())
    }

    override fun navigateToEditAddressScreen() {
        val id = selectedAddresses.first()
        val address = presenter?.state?.addresses?.find { it.id == id }
        if (address != null) {
            presenter?.onModeChanged(Mode.NONE)
            selectedAddresses.clear()
            findNavController().navigate(AddressesFragmentDirections.actionAddressesFragmentToEditAddressFragment(address))
        }
    }

    override fun copyAddress() {
        val id = selectedAddresses.first()

        copyToClipboard(id, copyTag)

        showSnackBar(getString(R.string.address_copied_to_clipboard))

        cancelSelectedAddresses()
    }

    override fun deleteAddresses() {
        presenter?.onDeleteAddress(selectedAddresses)
    }

    override fun showDeleteAddressesDialog(transactionAlert:Boolean) {
        if (transactionAlert) {
            context?.let {
                val view = LayoutInflater.from(it).inflate(R.layout.dialog_delete_address, null)
                val dialog = AlertDialog.Builder(it).setView(view).show()

                val contact = AppManager.instance.getAddress(selectedAddresses.first())

                val titleLabel = dialog.findViewById<TextView>(R.id.clearDialogTitle)
                val msgLabel = dialog.findViewById<TextView>(R.id.deleteAllTransactionsTitle)
                val confirm =  dialog.findViewById<TextView>(R.id.btnConfirm)

                confirm.setText(R.string.delete)

                if (contact?.isContact == true)
                {
                    if (selectedAddresses.count() > 1) {
                        titleLabel.text = getString(R.string.delete_contacts)
                        msgLabel.text = getString(R.string.delete_all_transactions_related_to_this_contacts)
                    }
                    else{
                        titleLabel.text = getString(R.string.delete_contact)
                        msgLabel.text = getString(R.string.delete_all_transactions_related_to_this_contact)
                    }
                }
                else{
                    if (selectedAddresses.count() > 1) {
                        titleLabel.text = getString(R.string.delete_addresses)
                        msgLabel.text = getString(R.string.delete_all_transactions_related_to_this_addresses)
                    }
                }

                view.btnConfirm.setOnClickListener {
                    showDeleteAddressesSnackBar(view.deleteAllTransactionsCheckbox.isChecked)
                    dialog.dismiss()
                }

                view.btnCancel.setOnClickListener { dialog.dismiss() }

                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        else{
            val contact = AppManager.instance.getAddress(selectedAddresses.first())

            val msgText = when {
                contact?.isContact == true -> when {
                    selectedAddresses.count() > 1 -> getString(R.string.delete_contacts_text)
                    else -> getString(R.string.delete_contact_text)
                }
                else ->  when {
                    selectedAddresses.count() > 1 -> getString(R.string.delete_addresses_text)
                    else -> getString(R.string.delete_address_text)
                }
            }

            val titleText = when {
                contact?.isContact == true -> when {
                    selectedAddresses.count() > 1 -> getString(R.string.delete_contacts)
                    else -> getString(R.string.delete_contact)
                }
                else ->  when {
                    selectedAddresses.count() > 1 -> getString(R.string.delete_addresses)
                    else -> getString(R.string.delete_address)
                }
            }


            showAlert(msgText,getString(R.string.delete),{
                showDeleteAddressesSnackBar(false)

            },titleText,getString(R.string.cancel))
        }
    }

    private fun showDeleteAddressesSnackBar(removeTransactions: Boolean) {
        val contact = AppManager.instance.getAddress(selectedAddresses.first())

        val text = when {
            contact?.isContact == true -> if (selectedAddresses.count() > 1) {
                getString(R.string.contacts_deleted)
            } else {
                getString(R.string.contact_deleted)
            }
            else -> if (selectedAddresses.count() > 1) {
                getString(R.string.addresses_deleted)
            } else {
                getString(R.string.address_deleted)
            }
        }

        showSnackBar(text,
                onDismiss = {
                    removedAddresses?.forEach { walletID ->
                        TrashManager.remove(walletID)
                    }
                },
                onUndo = {
                    removedAddresses?.forEach { walletID ->
                        TrashManager.restore(walletID)
                    }
                    removedAddresses?.clear()
                }
        )

        removedAddresses.clear()
        removedAddresses.addAll(selectedAddresses.toList())

        presenter?.onConfirmDeleteAddresses(removeTransactions, selectedAddresses.toList())

        cancelSelectedAddresses()
    }

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    override fun showAddressDetails(address: WalletAddress) {
        findNavController().navigate(AddressesFragmentDirections.actionAddressesFragmentToAddressFragment(address))
    }

    override fun updateAddresses(tab: Tab, addresses: List<WalletAddress>) {
        pagerAdapter.setData(tab, addresses)
    }

    override fun updatePlaceholder(showPlaceholder: Boolean) = if (showPlaceholder) {
        emptyLayout.visibility = View.VISIBLE
        pager.visibility = View.GONE
        tabLayout.visibility = View.INVISIBLE
    } else {
        emptyLayout.visibility = View.GONE
        pager.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
    }

    override fun changeMode(mode: Mode) {
        this.mode = mode
        tabLayout.setMode(mode)
        pager.setMode(mode)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AddressesPresenter(this, AddressesRepository(), AddressesState())
    }

}
