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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import android.os.Handler
import android.os.Looper
import io.reactivex.disposables.Disposable

/**
 *  2/28/19.
 */
class AddressesPresenter(currentView: AddressesContract.View, currentRepository: AddressesContract.Repository, val state: AddressesState)
    : BasePresenter<AddressesContract.View, AddressesContract.Repository>(currentView, currentRepository),
        AddressesContract.Presenter {

    private lateinit var addressesSubscription: Disposable
    private lateinit var publicOfflineSubscription: Disposable
    var isAllSelected = false

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onAddressPressed(address: WalletAddress) {
        if (isPublicOffline(address)) {
            view?.showPublicOfflineAddress()
        }
        else {
            view?.showAddressDetails(address)
        }
    }

    fun isPublicOffline(address: WalletAddress): Boolean {
        val publicOffline = AppManager.instance.publicOfflineAddress
        return publicOffline.isNotEmpty() && address.id == publicOffline
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.addresses.clear()
        state.addresses.addAll(AppManager.instance.getAllAddresses())

        addressesSubscription = AppManager.instance.subOnAddressesChanged.subscribe(){
            state.addresses.clear()
            state.addresses.addAll(AppManager.instance.getAllAddresses())
            updateView()
        }

        // Unlike most listener callbacks, onPublicAddress is delivered straight from the native
        // thread rather than through WalletListener's ui handler, so the list update has to be
        // marshalled. Main-looper post rather than AppActivity.self: no activity reference to
        // race with teardown, and view? is simply null once the fragment is gone.
        publicOfflineSubscription = AppManager.instance.subOnPublicAddress.subscribe {
            Handler(Looper.getMainLooper()).post {
                updateView()
            }
        }

        // One token per wallet that never expires, so this is asked for once and cached.
        if (AppManager.instance.publicOfflineAddress.isEmpty()) {
            AppManager.instance.getPublicAddress()
        }

        updateView()
    }

    override fun onAddContactPressed() {
        view?.navigateToAddContactScreen()
    }

    override fun onEditAddressPressed() {
        view?.navigateToEditAddressScreen()
    }

    override fun onCopyAddressPressed() {
        view?.copyAddress()
    }

    override fun onDeleteAddressesPressed() {
        view?.deleteAddresses()
    }

    override fun onSelectAll() {
        isAllSelected = !isAllSelected

        if(isAllSelected)
        {
            view?.didSelectAllAddresses(state.addresses)
        }
        else{
            view?.didUnSelectAllAddresses()
        }
    }

    override fun onDeleteAddress(selected: List<String>) {
        var showTransactionsAlert = false

        selected?.forEach { walletID ->
            if (state.getTransactions(walletID).count() > 0) {
                showTransactionsAlert = true
                return@forEach
            }
        }

        if (showTransactionsAlert) {
            view?.showDeleteAddressesDialog(true)
        } else {
            view?.showDeleteAddressesDialog(false)
        }
    }

    override fun onConfirmDeleteAddresses(withTransactions: Boolean, addresses: List<String>) {
        for (i in 0 until addresses.count()) {
            val id = addresses[i]
            val address = state?.addresses?.find { it.id == id }
            if (address != null) {
                repository.deleteAddress(address, if (withTransactions) state?.getTransactions(id) else listOf())
            }
        }
    }

    private fun updateView() {
        // Through state.filteredAddresses so the rendered rows and the select-all count agree —
        // the active tab carries the pinned public offline entry, which is not in state.addresses.
        view?.updateAddresses(Tab.ACTIVE, state.filteredAddresses(Tab.ACTIVE.value))
        view?.updateAddresses(Tab.EXPIRED, state.filteredAddresses(Tab.EXPIRED.value))
        view?.updateAddresses(Tab.CONTACTS, state.filteredAddresses(Tab.CONTACTS.value))
        view?.updatePlaceholder(state.addresses.count() == 0)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, publicOfflineSubscription)

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true

    override fun onModeChanged(mode: AddressesFragment.Mode) {
        view?.changeMode(mode)
    }
}
