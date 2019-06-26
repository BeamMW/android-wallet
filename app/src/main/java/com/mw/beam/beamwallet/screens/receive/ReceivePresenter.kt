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

package com.mw.beam.beamwallet.screens.receive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, private val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {
    private lateinit var walletIdSubscription: Disposable
    private val changeAddressLiveData = MutableLiveData<WalletAddress>()


    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()

        val address = view?.getWalletAddressFromArguments()

        initViewAddress(address)

        val amount = view?.getAmountFromArguments()
        if (amount != null && amount > 0) {
            view?.setAmount(amount.convertToBeam())
        }

        changeAddressLiveData.observe(view!!.getLifecycleOwner(), Observer {
            state.address = it
            initViewAddress(it)
        })
    }

    private fun initViewAddress(address: WalletAddress?) {
        if (address != null) {
            state.address = address
            state.expirePeriod = if (address.duration == 0L) ExpirePeriod.NEVER else ExpirePeriod.DAY
            state.isNeedGenerateAddress = false
            state.wasAddressSaved = true
        }

        state.address?.let {
            view?.initAddress(state.isNeedGenerateAddress, it)
            view?.configCategory(repository.getCategory(it.walletID))
        }

    }

    override fun onCommentChanged() {
        saveAddress()
    }

    override fun onResume() {
        super.onResume()
        state.address?.let {
            view?.initAddress(state.isNeedGenerateAddress, it)
            view?.configCategory(repository.getCategory(it.walletID))
        }
        view?.handleExpandAdvanced(state.expandAdvanced)
        view?.handleExpandEditAddress(state.expandEditAddress)
    }

    override fun onShareTokenPressed() {
        saveAddress()

        if (state.address != null) {
            view?.shareToken(state.address!!.walletID)
        }
    }

    override fun onChangeAddressPressed() {
        saveAddress()
        view?.showChangeAddressFragment()
    }

    override fun onAdvancedPressed() {
        state.expandAdvanced = !state.expandAdvanced
        view?.handleExpandAdvanced(state.expandAdvanced)
    }

    override fun onEditAddressPressed() {
        state.expandEditAddress = !state.expandEditAddress
        view?.handleExpandEditAddress(state.expandEditAddress)
    }

    override fun onShowQrPressed() {
        saveAddress()
        state.address?.let { address ->
            view?.showQR(address, view?.getAmount()?.convertToGroth())
        }
    }

    override fun onExpirePeriodChanged(period: ExpirePeriod) {
        state.expirePeriod = period
        saveAddress()
    }

    override fun onAddNewCategoryPressed() {
        view?.showAddNewCategory()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletIdSubscription = if (state.isNeedGenerateAddress) {
            repository.generateNewAddress().subscribe {
                if (state.address == null) {
                    state.address = it
                    view?.initAddress(true, it)
                    view?.configCategory(repository.getCategory(it.walletID))
                    saveAddress()
                }
            }
        } else {
            EmptyDisposable()
        }
    }

    override fun onAddressChanged(walletAddress: WalletAddress) {
        state.isNeedGenerateAddress = false
        state.wasAddressSaved = true
        changeAddressLiveData.postValue(walletAddress)
    }

    override fun onSelectedCategory(category: Category?) {
        state.address?.let { repository.changeCategoryForAddress(it.walletID, category) }
        saveAddress()
    }

    private fun saveAddress() {
        if (state.address != null) {
            state.address!!.duration = state.expirePeriod.value

            val comment = view?.getComment()

            state.address!!.label = comment ?: ""

            if (state.wasAddressSaved) {
                repository.updateAddress(state.address!!)
            } else {
                repository.saveAddress(state.address!!)
            }

            state.wasAddressSaved = true
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletIdSubscription)

    override fun hasStatus(): Boolean = true
}
