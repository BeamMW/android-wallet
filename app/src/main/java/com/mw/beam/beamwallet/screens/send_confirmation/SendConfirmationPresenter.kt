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
            showWallet()
        }
    }

    override fun initSubscriptions() {
        addressesSubscription = repository.getAddresses().subscribe { data ->
            data.addresses?.forEach { address ->
                state.addresses[address.id] = address
            }

            var finder = state.token
            var out = state.outgoingAddress

            val findAddress = AppManager.instance.getAddress(finder)
            if (findAddress != null) {
                state.contact = findAddress
                view?.configureContact(findAddress)
            }

            val outAddress =  AppManager.instance.getAddress(out)
            //state.addresses.values.find { it.id == out || it.address == out}
            if (outAddress != null) {
                view?.configureOutAddress(outAddress)
            }
        }

        val totalSendAmount = state.amount + state.fee

        if (AppManager.instance.getStatus().shielded == 0L) {
            changeSubscription = repository.calcChange(totalSendAmount).subscribe {
                view?.configUtxoInfo((totalSendAmount).convertToBeam(), it.convertToBeam())
            }
        }
        else {
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
                    var change = it.change

                    if (isShielded) {
                        change += it.shieldedInputsFee
                    }
                    state.shieldedInputsFee = it.shieldedInputsFee

                    val left = 0L //AppManager.instance.getStatus().available - totalSendAmount
                    if (left < change) {
                        change = 0L
                    }
                    view?.configUtxoInfo((totalSendAmount).convertToBeam(), change.convertToBeam())
                }
            }

            AppManager.instance.wallet?.calcShieldedCoinSelectionInfo(state.amount, state.fee, isShielded)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, changeSubscription)

    private fun showWallet() {
        state.apply { view?.delaySend(outgoingAddress, state.token, comment, amount, fee - shieldedInputsFee, view?.getIsOffline()) }
        view?.showWallet()
    }
}