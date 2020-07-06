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

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.subscribeIf
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable

/**
 *  11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, private val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {

    enum class ReceiveOptions(val value: Int) {
        WALLET(0), POOL(1)
    }

    enum class ExpireOptions(val value: Long) {
        ONETIME(86400), PERMANENT(0)
    }

    private var categorySubscription: Disposable? = null

    var expire = ExpireOptions.PERMANENT
    var receive = ReceiveOptions.WALLET

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init()

        val address = view?.getWalletAddressFromArguments()

        if(address!=null) {
            initViewAddress(address)

            val amount = view?.getAmountFromArguments()
            if (amount != null && amount > 0) {
                view?.setAmount(amount.convertToBeam())
            }
        }
        else {
            AppActivity.self.runOnUiThread {
                val dto = AppManager.instance.wallet?.generateToken()
                if (dto!=null) {
                    val token = WalletAddress(dto)
                    initViewAddress(token)
                }
            }
        }

    }

    override fun onDestroy() {
        categorySubscription?.dispose()

        super.onDestroy()
    }

    fun setAddressName(name:String) {
        state.address?.label = name
    }

    fun setTags(tags: List<Tag>) {
        state.tags.clear()
        state.tags.addAll(tags)
    }

    override fun onAddressLongPressed() {
        saveAddress()
        view?.vibrate(100)
        view?.copyAddress(state.address?.walletID ?: "")
    }

    private fun initViewAddress(address: WalletAddress?) {
        if (address != null) {
            state.address = address
        }

        state.address?.let {
            view?.initAddress(it, expire, receive)

            if (state.tags.count() != 0 ) {
                view?.setTags(state.tags)
            }
            else{
                view?.setTags(repository.getAddressTags(it.walletID))
            }
        }

    }

    override fun onResume() {
        super.onResume()

        view?.setupTagAction(repository.getAllTags().isEmpty())
        view?.handleExpandAdvanced(state.expandAdvanced)
        view?.handleExpandEditAddress(state.expandEditAddress)
    }

    override fun onBackPressed() {
        requestSaveAddress {
            view?.close()
        }
    }

    private fun requestSaveAddress(nextStep: () -> Unit) {
        if (isAddressInfoChanged() && !state.wasAddressSaved) {
            view?.showSaveChangesDialog(nextStep)
        }
        else if (!state.wasAddressSaved) {
            view?.showSaveAddressDialog(nextStep)
        }
        else if (isAddressInfoChanged()) {
            view?.showSaveChangesDialog(nextStep)
        }
        else {
            saveAddress()
            nextStep()
        }
    }

    override fun onSaveAddressPressed() {
        saveAddress()
    }

    override fun onShareTokenPressed() {
        if (state.address != null) {
            saveAddress()
            if (receive == ReceiveOptions.POOL) {
                view?.shareToken(state.address!!.walletID)
            } else {
                view?.shareToken(state.address!!.token)
            }
        }
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


    override fun onTagActionPressed() {
        if (repository.getAllTags().isEmpty()) {
            view?.showCreateTagDialog()
        } else {
            view?.showTagsDialog(state.tags)
        }
    }

    private fun isAddressInfoChanged(): Boolean {
        val savedTags = state.address?.walletID?.let { repository.getAddressTags(it) }

        if (state.address?.label != view?.getComment()) {
            return true
        }
        else if (state.address?.duration != expire.value) {
            return true
        }
        else if (state.tags.size != savedTags?.size) {
            return true
        }
        else if (!state.tags.containsAll(savedTags) && state.tags.count() > 0) {
            return true
        }
        return false
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        if (categorySubscription==null)
        {
            categorySubscription = TagHelper.subOnCategoryCreated.subscribe(){
                if (it!=null) {
                    state.tags.clear()
                    state.tags.add(it)
                }
            }
        }
    }


    override fun onSelectTags(tags: List<Tag>) {
        state.tags.clear()
        state.tags.addAll(tags)
        view?.setTags(tags)
    }

    override fun onTokenPressed() {
        if (state.address != null) {
            if (receive == ReceiveOptions.POOL) {
                view?.showShowToken(state.address!!.walletID)
            } else {
                view?.showShowToken(state.address!!.token)
            }
        }
    }

    private fun saveAddress() {
        state.address?.let { address ->
            address.duration = expire.value

            val comment = view?.getComment()
            address.label = comment ?: ""

            view?.getTxComment()?.let {
                if (it.isNotBlank()) {
                    ReceiveTxCommentHelper.saveCommentToAddress(address.walletID, it)
                }
            }

            repository.saveAddress(address, state.tags)
        }
    }

    override fun onCreateNewTagPressed() {
        view?.showAddNewCategory()
    }

    override fun onPermanentPressed() {
        expire = ExpireOptions.PERMANENT
        initViewAddress(state?.address)
    }

    override fun onOneTimePressed() {
        expire = ExpireOptions.ONETIME
        initViewAddress(state?.address)
    }

    override fun onWalletPressed() {
        receive = ReceiveOptions.WALLET
        initViewAddress(state?.address)
    }

    override fun onPoolPressed() {
        receive = ReceiveOptions.POOL
        initViewAddress(state?.address)
    }

    override fun hasStatus(): Boolean = true
}
