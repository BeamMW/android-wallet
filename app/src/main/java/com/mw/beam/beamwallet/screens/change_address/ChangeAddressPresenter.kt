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

package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

class ChangeAddressPresenter(view: ChangeAddressContract.View?, repository: ChangeAddressContract.Repository, private val state: ChangeAddressState)
    : BasePresenter<ChangeAddressContract.View, ChangeAddressContract.Repository>(view, repository), ChangeAddressContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.viewState = if (view?.isFromReceive() != false) ChangeAddressContract.ViewState.Receive else ChangeAddressContract.ViewState.Send
        state.generatedAddress = view?.getGeneratedAddress()

        view?.init(state.viewState, state.generatedAddress)

    }

    override fun onStart() {
        super.onStart()

        if (state.scannedAddress != null) {
            state.scannedAddress?.let { view?.setAddress(it) }

            state.scannedAddress = null
        }
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            val addresses = it.addresses?.filter { walletAddress -> !walletAddress.isContact && !walletAddress.isExpired }
            state.updateAddresses(addresses)
            state.deleteAddresses(repository.getAllAddressesInTrash())

            onChangeSearchText(view?.getSearchText() ?: "")
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    state.deleteAddresses(it.data.addresses)
                    onChangeSearchText(view?.getSearchText() ?: "")
                }

                TrashManager.ActionType.Restored -> {
                    state.updateAddresses(it.data.addresses)
                    onChangeSearchText(view?.getSearchText() ?: "")
                }

                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, trashSubscription)

    override fun onChangeSearchText(text: String) {
        if (text.isBlank()) {
            view?.updateList(state.getAddresses())
            return
        }
        
        val searchText = text.trim().toLowerCase()
        
        val newItems = state.getAddresses().filter {
            it.label.trim().toLowerCase().contains(searchText) ||
                    it.walletID.trim().toLowerCase().startsWith(searchText) ||
                    repository.getCategoryForAddress(it.walletID)?.name?.toLowerCase()?.contains(searchText) ?: false
        }

        view?.updateList(newItems)
    }

    override fun onItemPressed(walletAddress: WalletAddress) {
        view?.back(walletAddress)
    }
}