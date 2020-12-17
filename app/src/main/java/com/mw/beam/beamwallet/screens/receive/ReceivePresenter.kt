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

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.core.utils.subscribeIf
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.Subject
import java.util.*
import kotlin.concurrent.schedule

/**
 *  11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {

    enum class TransactionTypeOptions(val value: Int) {
        REGULAR(0), MAX_PRIVACY(1)
    }

    enum class TokenExpireOptions(val value: Long) {
        ONETIME(86400), PERMANENT(0)
    }

    private var categorySubscription: Disposable? = null
    private lateinit var walletIdSubscription: Disposable
    private lateinit var maxAddressSubscription: Disposable

    var expire = TokenExpireOptions.ONETIME
    var transaction = TransactionTypeOptions.REGULAR
    var isSkipSave = false

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init()

        val address = view?.getWalletAddressFromArguments()

        if(address!=null) {
            isSkipSave = true
            state.wasAddressSaved = true
            state.address = address
            state.isNeedGenerateAddress = false
            initViewAddress(address)

            updateToken()

            val amount = view?.getAmountFromArguments()
            if (amount != null && amount > 0) {
                view?.setAmount(amount.convertToBeam())
            }
        }
        else if(state.address != null) {
            initViewAddress(state?.address)
            view?.updateTokens(state?.address!!)
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
            view?.initAddress(it, transaction, expire)

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
        if (isSkipSave) {
            nextStep()
        }
        else {
            if (isAddressInfoChanged() && !state.wasAddressSaved) {
                view?.showSaveChangesDialog(nextStep)
            }
            else if (!state.wasAddressSaved) {
                view?.showSaveAddressDialog(nextStep)
            }
//            else if (isAddressInfoChanged()) {
//                view?.showSaveChangesDialog(nextStep)
//            }
            else {
                saveAddress()
                nextStep()
            }
        }
    }

    override fun onSaveAddressPressed() {
        saveAddress()
    }

    override fun onShareTokenPressed() {
        if (transaction == TransactionTypeOptions.MAX_PRIVACY) {
            view?.shareToken(state.address!!.tokenMaxPrivacy)
        }
        else {
            val option1 = App.self.resources.getString(R.string.online_token) + " (" + App.self.resources.getString(R.string.for_wallet).toLowerCase() + ")"
            val option2 = App.self.resources.getString(R.string.online_token) + " (" + App.self.resources.getString(R.string.for_pool).toLowerCase() + ")"
            val option3 = App.self.resources.getString(R.string.offline_token)
            view?.showShareDialog(option1, option2, option3)
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

    override fun onShowQrPressed(receiveToken: String) {
        view?.showQR(receiveToken)
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

        if (categorySubscription == null)
        {
            categorySubscription = TagHelper.subOnCategoryCreated.subscribe(){
                if (it!=null) {
                    state.tags.clear()
                    state.tags.add(it)
                }
            }
        }

        walletIdSubscription = repository.generateNewAddress().subscribeIf(state.isNeedGenerateAddress) {
            if (state.address == null) {
                AppManager.instance.wallet?.saveAddress(it.toDTO(), true)

                state.address = it
                state.generatedAddress = it
                updateToken()
                initViewAddress(state.address)
                view?.setTags(repository.getAddressTags(it.walletID))
                state.isNeedGenerateAddress = false
            }
        }


        maxAddressSubscription = AppManager.instance.subOnMaxPrivacyAddress.subscribe {
            state.address?.tokenMaxPrivacy = it
            view?.updateTokens(state?.address!!)
        }
    }


    override fun onSelectTags(tags: List<Tag>) {
        state.tags.clear()
        state.tags.addAll(tags)
        view?.setTags(tags)
    }

    override fun onTokenPressed(receiveToken: String) {
        view?.showShowToken(receiveToken)

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
        expire = TokenExpireOptions.PERMANENT
        state.address?.duration = expire.value
        updateToken()
        saveAddress()
        initViewAddress(state?.address)
    }

    override fun onOneTimePressed() {
        expire = TokenExpireOptions.ONETIME
        state.address?.duration = expire.value
        updateToken()
        saveAddress()
        initViewAddress(state?.address)
    }

    override fun onMaxPrivacyPressed() {
        transaction = TransactionTypeOptions.MAX_PRIVACY
        updateToken()
        initViewAddress(state?.address)
    }

    override fun onRegularPressed() {
        transaction = TransactionTypeOptions.REGULAR
        updateToken()
        initViewAddress(state?.address)
    }

    override fun onSwitchPressed() {
        transaction = TransactionTypeOptions.REGULAR
        expire = TokenExpireOptions.PERMANENT
        state.address?.duration = expire.value
        saveAddress()
        updateToken()
        initViewAddress(state?.address)
    }

    override fun updateToken() {
        requestAddresses()
    }

    private fun requestAddresses() {
        if(state?.address != null) {
            var amount = view?.getAmount()?.convertToGroth()
            if(amount == null) {
                amount = 0L
            }
            state?.address?.tokenOnline = AppManager.instance.wallet?.generateRegularAddress(expire == TokenExpireOptions.PERMANENT, amount, state!!.address!!.walletID)!!
            state?.address?.tokenOffline = AppManager.instance.wallet?.generateOfflineAddress(amount, state!!.address!!.walletID)!!
            AppManager.instance.wallet?.generateMaxPrivacyAddress(amount, state!!.address!!.walletID)

            view?.updateTokens(state?.address!!)
        }

    }

    override fun hasStatus(): Boolean = true

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletIdSubscription, maxAddressSubscription)
}
