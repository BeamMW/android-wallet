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
import com.google.android.material.navigation.NavigationView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.screens.wallet.NavItem
import kotlinx.android.synthetic.main.dialog_delete_address.view.*
import kotlinx.android.synthetic.main.fragment_addresses.*
import kotlinx.android.synthetic.main.fragment_addresses.toolbarLayout

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
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
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
                            if (selectedAddresses.contains(item.walletID)) {
                                selectedAddresses.remove(item.walletID)
                            } else {
                                selectedAddresses.add(item.walletID)
                            }

                            onSelectedAddressesChanged()
                        }
                    }
                },
                object : AddressesAdapter.OnLongClickListener {
                    override fun onLongClick(item: WalletAddress) {
                        if (mode == Mode.NONE) {

                            presenter?.onModeChanged(Mode.EDIT)

                            selectedAddresses.add(item.walletID)

                            pagerAdapter.changeSelectedItems(selectedAddresses, true, item.walletID)

                            pagerAdapter.reloadData(mode)

                            onSelectedAddressesChanged()
                        }
                    }
                },

                {
                    return@AddressesPagerAdapter presenter?.onSearchTagsForAddress(it) ?: listOf()
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
        configNavView(toolbarLayout, navView as NavigationView, drawerLayout, NavItem.ID.ADDRESS_BOOK)
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
                showWalletFragment()
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
        } else {
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun navigateToAddContactScreen() {
        findNavController().navigate(AddressesFragmentDirections.actionAddressesFragmentToAddContactFragment())
    }

    override fun navigateToEditAddressScreen() {
        val id = selectedAddresses.first()
        val address = presenter?.state?.addresses?.find { it.walletID == id }
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

    override fun showDeleteAddressesDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_delete_address, null)
            val dialog = AlertDialog.Builder(it).setView(view).show()

            if (selectedAddresses.count() > 1) {
                val titleLabel = dialog.findViewById<TextView>(R.id.clearDialogTitle)
                titleLabel.text = getString(R.string.delete_addresses)

                val msgLabel = dialog.findViewById<TextView>(R.id.deleteAllTransactionsTitle)
                msgLabel.text = getString(R.string.delete_all_transactions_related_to_this_addresses)
            }

            view.btnConfirm.setOnClickListener {
                showDeleteAddressesSnackBar(view.deleteAllTransactionsCheckbox.isChecked)
                dialog.dismiss()
            }

            view.btnCancel.setOnClickListener { dialog.dismiss() }

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun showDeleteAddressesSnackBar(removeTransactions: Boolean) {
        presenter?.onConfirmDeleteAddresses(removeTransactions, selectedAddresses.toList())

        val text = if (selectedAddresses.count() > 1) {
            getString(R.string.addresses_deleted)
        } else {
            getString(R.string.address_deleted)
        }

        showSnackBar(text,
                onDismiss = {
                    presenter?.removedAddresses?.forEach { walletID ->
                        TrashManager.remove(walletID)
                    }
                },
                onUndo = {
                    presenter?.removedAddresses?.forEach { walletID ->
                        TrashManager.restore(walletID)
                    }
                    presenter?.removedAddresses?.clear()
                }
        )

        cancelSelectedAddresses()
    }

    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

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
