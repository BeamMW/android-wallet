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
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 3/4/19.
 */
class AddressPresenter(currentView: AddressContract.View, currentRepository: AddressContract.Repository, private val state: AddressState)
    : BasePresenter<AddressContract.View, AddressContract.Repository>(currentView, currentRepository),
        AddressContract.Presenter {
    private val COPY_TAG = "ADDRESS"
    private lateinit var txStatusSubscription: Disposable


    override fun onViewCreated() {
        super.onViewCreated()
        state.address = view?.getAddress()
        view?.init(state.address ?: return)
    }

    override fun onStart() {
        super.onStart()
        notifyPrivacyStateChange()
        view?.configureCategory(repository.getCategory(state.address?.walletID ?: return))
    }

    private fun notifyPrivacyStateChange() {
        view?.configPrivacyStatus(repository.isPrivacyModeEnabled())
    }

    override fun onShowQR() {
        view?.showQR(state.address ?: return)
    }

    override fun onCopyAddress() {
        view?.copyToClipboard(state.address?.walletID ?: return, COPY_TAG)
    }

    override fun onMenuCreate(menu: Menu?) {
        view?.configMenuItems(menu, state.address ?: return)
    }

    override fun onEditAddress() {
        view?.showEditAddressScreen(state.address ?: return)
    }

    override fun onAddressWasEdited() {
        view?.finishScreen()
    }

    override fun onDeleteAddress() {
        repository.deleteAddress(state.address?.walletID ?: return)
        view?.finishScreen()
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription)
    }

    override fun onDialogSharePressed() {
        if (state.address != null) {
            view?.shareToken(state.address!!.walletID)
            view?.dismissDialog()
        }
    }

    override fun onDialogClosePressed() {
        view?.dismissDialog()
    }

    override fun onDestroy() {
        view?.dismissDialog()
        super.onDestroy()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            view?.configTransactions(
                    when (data.action) {
                        ChangeAction.REMOVED -> state.deleteTransaction(data.tx)
                        else -> state.updateTransactions(data.tx)
                    })
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription)
}
