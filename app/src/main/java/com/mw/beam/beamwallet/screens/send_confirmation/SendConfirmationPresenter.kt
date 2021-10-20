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

package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable

class SendConfirmationPresenter(view: SendConfirmationContract.View?, repository: SendConfirmationContract.Repository, private val state: SendConfirmationState)
    : BasePresenter<SendConfirmationContract.View, SendConfirmationContract.Repository>(view, repository), SendConfirmationContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var changeSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.token = getAddress()
            state.addressType = getAddressType()
            state.outgoingAddress = getOutgoingAddress()
            state.amount = getAmount()
            state.fee = getFee()
            state.comment = getComment()

            init(state.token, state.outgoingAddress, state.amount.convertToBeam(), state.fee, state.addressType)
        }
    }

    override fun onSendPressed() {
        if (repository.isConfirmTransactionEnabled()) {
            view?.showConfirmDialog()
        } else {
            send()
        }
    }

    override fun onConfirmed() {
        send()
    }

    private fun send() {
        if (state.contact == null) {
            state.apply { view?.delaySend(outgoingAddress, token, comment, amount, fee - shieldedInputsFee, view?.getIsOffline()) }
            view?.showSaveAddressFragment(state.token)
        } else {
            AppManager.lastSendSavedContact = state.contact?.toDTO()
            showWallet()
        }
    }

    override fun initSubscriptions() {
        addressesSubscription = repository.getAddresses().subscribe { data ->
            data.addresses?.forEach { address ->
                state.addresses[address.id] = address
            }

            val finder = state.token
            val out = state.outgoingAddress

            val findAddress = AppManager.instance.getAddress(finder)
            if (findAddress != null) {
                state.contact = findAddress
                view?.configureContact(findAddress)
            }

            val outAddress =  AppManager.instance.getAddress(out)
            if (outAddress != null) {
                view?.configureOutAddress(outAddress)
            }
        }

        var totalSendAmount = state.amount
        if(view?.getAssetId() == 0) {
            totalSendAmount += state.fee
        }

        if (view?.getChange() == null)
        {
            view?.configUtxoInfo((totalSendAmount).convertToBeam(), 0L.convertToBeam())
        }
        else {
            view?.configUtxoInfo((totalSendAmount).convertToBeam(), view!!.getChange().convertToBeam())
        }

        val isShielded = (view?.getIsOffline() == true || state.getEnumAddressType()  == BMAddressType.BMAddressTypeOfflinePublic ||
                state.getEnumAddressType()  == BMAddressType.BMAddressTypeMaxPrivacy)

        changeSubscription = WalletListener.subOnFeeCalculated.subscribe {
            AppActivity.self.runOnUiThread {
                val change = it.change
                view?.configUtxoInfo((totalSendAmount).convertToBeam(), change.convertToBeam())
            }
        }

        AppManager.instance.wallet?.selectCoins(state.amount, state.fee, isShielded, view?.getAssetId() ?: 0)
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(addressesSubscription, changeSubscription)

    private fun showWallet() {
        state.apply { view?.delaySend(outgoingAddress, state.token, comment, amount, fee - shieldedInputsFee, view?.getIsOffline()) }
        view?.showWallet()
    }
}