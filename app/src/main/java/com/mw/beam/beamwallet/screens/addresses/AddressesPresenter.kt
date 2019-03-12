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
import com.mw.beam.beamwallet.core.entities.WalletAddress
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesPresenter(currentView: AddressesContract.View, currentRepository: AddressesContract.Repository)
    : BasePresenter<AddressesContract.View, AddressesContract.Repository>(currentView, currentRepository),
        AddressesContract.Presenter {
    private lateinit var addressesSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onAddressPressed(address: WalletAddress) {
        view?.showAddressDetails(address)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            if (it.own) {
                view?.updateAddresses(Tab.ACTIVE, it.addresses?.filter { address -> !address.isExpired } ?: listOf())
                view?.updateAddresses(Tab.EXPIRED, it.addresses?.filter { address -> address.isExpired } ?: listOf())
            } else {
                view?.updateAddresses(Tab.CONTACTS, it.addresses ?: listOf())
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription)

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
