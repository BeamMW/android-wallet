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

import android.view.Menu
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import io.reactivex.disposables.Disposable

/**
 *  3/4/19.
 */
class AddressPresenter(currentView: AddressContract.View, currentRepository: AddressContract.Repository, private val state: AddressState)
    : BasePresenter<AddressContract.View, AddressContract.Repository>(currentView, currentRepository),
        AddressContract.Presenter {

    private val COPY_TAG = "ADDRESS"
    private lateinit var txStatusSubscription: Disposable
    private lateinit var addressSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.address = AppManager.instance.getAddress(view?.getAddress()?.id)

        view?.init(state.address ?: return)
    }

    override fun onStart() {
        super.onStart()
        notifyPrivacyStateChange()
    }

    private fun notifyPrivacyStateChange() {
        view?.configPrivacyStatus(repository.isPrivacyModeEnabled())
    }

    override fun onShowQR() {
        view?.showQR(state.address ?: return)
    }

    override fun onCopyAddress() {
        val copyValue = state.address?.displayAddress ?: state.address?.address
        view?.copyToClipboard(copyValue ?: return, COPY_TAG)
    }

    override fun onMenuCreate(menu: Menu?) {
        view?.configMenuItems(menu, state.address ?: return)
    }

    override fun onEditAddress() {
        view?.showEditAddressScreen(state.address ?: return)
    }

    override fun onReceiveAddress() {
        view?.receiveAddress(state.address ?: return)
    }

    override fun onSendAddress() {
        view?.sendAddress(state.address ?: return)
    }

    override fun onAddressWasEdited() {
        view?.finishScreen()
    }

    override fun onDeleteAddress() {
        if (state.getTransactions().isNotEmpty()) {
            view?.showDeleteAddressDialog()
        } else {
            onConfirmDeleteAddress(false)
        }
    }

    override fun onConfirmDeleteAddress(withTransactions: Boolean) {
        state.address?.let {
            view?.showDeleteSnackBar(it)
            repository.deleteAddress(it, if (withTransactions) state.getTransactions() else listOf())
            view?.finishScreen()
        }
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.updateTransactions(AppManager.instance.getTransactionsByAddress(state.address?.id))
        view?.configTransactions(state.getTransactions())

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            state.updateTransactions(AppManager.instance.getTransactionsByAddress(state.address?.id))
            view?.configTransactions(state.getTransactions())
        }

        addressSubscription = AppManager.instance.subOnAddressesChanged.subscribe {
            if (it == true && view?.getAddress()?.isContact == false) {
                state.address = AppManager.instance.getAddress(view?.getAddress()?.id)
                state.address?.let { it1 -> view?.init(it1) }
            }
            else if (it == false && view?.getAddress()?.isContact == true) {
                state.address = AppManager.instance.getAddress(view?.getAddress()?.id)
                state.address?.let { it1 -> view?.init(it1) }
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription,addressSubscription)
}
