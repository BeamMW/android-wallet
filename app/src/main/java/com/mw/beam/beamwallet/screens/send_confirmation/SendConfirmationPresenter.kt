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
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import io.reactivex.disposables.Disposable

class SendConfirmationPresenter(view: SendConfirmationContract.View?, repository: SendConfirmationContract.Repository, private val state: SendConfirmationState)
    : BasePresenter<SendConfirmationContract.View, SendConfirmationContract.Repository>(view, repository), SendConfirmationContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var changeSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.token = getAddress()
            state.maxPrivacy = getMaxPrivacy()
            state.outgoingAddress = getOutgoingAddress()
            state.amount = getAmount()
            state.isOffline = getOffline()
            state.fee = getFee()
            state.comment = getComment()

            init(state.token, state.outgoingAddress, state.amount.convertToBeam(), state.fee, state.maxPrivacy, state.isOffline)
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
            state.apply { view?.delaySend(outgoingAddress, token, comment, amount, fee, maxPrivacy) }
            view?.showSaveAddressFragment(state.token)
        } else {
            showWallet()
        }
    }

    override fun initSubscriptions() {
        addressesSubscription = repository.getAddresses().subscribe {
            it.addresses?.forEach { address ->
                state.addresses[address.walletID] = address
            }

            val findAddress = state.addresses.values.find { it.walletID == state.token }
            if (findAddress != null) {
                state.contact = findAddress
                view?.configureContact(findAddress, repository.getAddressTags(findAddress.walletID))
            }
        }

        val totalSendAmount = state.amount + state.fee
        changeSubscription = repository.calcChange(totalSendAmount).subscribe {
            view?.configUtxoInfo((it + totalSendAmount).convertToBeam(), it.convertToBeam())
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, changeSubscription)

    private fun showWallet() {
        state.apply { view?.delaySend(outgoingAddress, token, comment, amount, fee, maxPrivacy) }
        view?.showWallet()
    }
}