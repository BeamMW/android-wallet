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

import android.os.Handler
import android.os.Looper

import io.reactivex.disposables.Disposable

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import org.jetbrains.anko.runOnUiThread

class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {

    enum class TransactionTypeOptions(val value: Int) {
        REGULAR(0), MAX_PRIVACY(1)
    }

    private lateinit var walletIdSubscription: Disposable
    private lateinit var maxAddressSubscription: Disposable
    private lateinit var regularAddressSubscription: Disposable
    private lateinit var offlineAddressSubscription: Disposable

    var transaction = TransactionTypeOptions.REGULAR
    private var isSkipSave = true
    private var oldAmount = 0L
    private var oldAssetId = 0
    private var forceRequest = false
    private var firstLoad = false
    var isSBBS = false

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init()

        val address = view?.getWalletAddressFromArguments()

        if(address!=null) {
            var paramsAmount = 0L
            if (!firstLoad) {
                AppManager.instance.wallet?.clearLastWalletId()

                firstLoad = true

                forceRequest = true

                val isToken = AppManager.instance.wallet?.isToken(address.address)
                val canSend = AppManager.instance.isMaxPrivacyEnabled()

                if (isToken == true && canSend) {
                    val params = AppManager.instance.wallet?.getTransactionParameters(address.address, false)
                    if (params?.isMaxPrivacy == true) {
                        transaction = TransactionTypeOptions.MAX_PRIVACY
                    }
                    paramsAmount = params?.amount ?: 0L
                }
                else {
                    isSBBS = !canSend
                }

                if (paramsAmount > 0L) {
                    view?.setAmount(paramsAmount.convertToBeam())
                }
            }

            initSubscriptions()

            isSkipSave = true
            state.wasAddressSaved = true
            state.address = address
            initViewAddress(address)


            updateToken()

            val amount = view?.getAmountFromArguments() ?: paramsAmount
            if (amount > 0) {
                view?.setAmount(amount.convertToBeam())
            }

//            saveToken()
        }
        else if(state.address != null) {
            initViewAddress(state.address)
            view?.updateTokens(state.address!!, transaction)
        }
    }

    fun setAddressName(name:String) {
        state.address?.label = name
    }

//    override fun saveToken() {
//        state.address?.let { address ->
//            val comment = view?.getComment()
//            address.label = comment ?: ""
//
//            if(transaction == TransactionTypeOptions.MAX_PRIVACY) {
//                address.address = address.tokenMaxPrivacy
//            }
//            else {
//                if(AppManager.instance.isMaxPrivacyEnabled()) {
//                    address.address = address.tokenOffline
//                }
//                else {
//                    address.address = address.address
//                }
//            }
//
//            repository.saveAddress(address)
//        }
//    }

    private fun initViewAddress(address: WalletAddress?) {
        if (address != null) {
            state.address = address
        }

        state.address?.let {
            view?.initAddress(it, transaction)
        }

    }

    override fun onResume() {
        super.onResume()
        view?.handleExpandAmount(state.expandAmount)
        view?.handleExpandComment(state.expandComment)
        view?.handleExpandAdvanced(state.expandAdvanced)
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
            else {
//                saveAddress()
                nextStep()
            }
        }
    }

    override fun onSaveAddressPressed() {
//        saveAddress()
    }

    override fun onShareTokenPressed() {
        if (transaction == TransactionTypeOptions.MAX_PRIVACY) {
            view?.shareToken(state.address?.tokenMaxPrivacy ?: "")
        }
        else {
            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                view?.shareToken(state.address?.id ?: "")
            }
            else {
                view?.shareToken(state.address?.tokenOffline ?: "")
            }
        }
    }

    override fun onCommentPressed() {
        state.expandComment = !state.expandComment
        view?.handleExpandComment(state.expandComment)
    }

    override fun onAmountPressed() {
        state.expandAmount = !state.expandAmount
        view?.handleExpandAmount(state.expandAmount)
    }

    override fun onAdvancedPressed() {
        state.expandAdvanced = !state.expandAdvanced
        view?.handleExpandAdvanced(state.expandAdvanced)
    }

    override fun onShowQrPressed() {
        if (transaction == TransactionTypeOptions.MAX_PRIVACY) {
            view?.showQR(state.address?.tokenMaxPrivacy ?: "")
        }
        else {
            if (!AppManager.instance.isMaxPrivacyEnabled()) {
                view?.showQR(state.address?.id ?: "")
            }
            else {
                view?.showQR(state.address?.tokenOffline ?: "")
            }
        }
    }

    override fun onCopyPressed() {
        if (transaction == TransactionTypeOptions.MAX_PRIVACY) {
            view?.copyToken(state.address?.tokenMaxPrivacy ?: "")
        }
        else {
            if(!AppManager.instance.isMaxPrivacyEnabled()) {
                view?.copyToken(state.address?.id ?: "")
            }
            else {
                view?.copyToken(state.address?.tokenOffline ?: "")
            }
        }
    }

    private fun isAddressInfoChanged(): Boolean {
        if (state.address?.label != view?.getComment()) {
            return true
        }
        return false
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletIdSubscription = AppManager.instance.subOnAddressCreated.subscribe {
            if (state.address == null) {
                it.duration = ((24 * 60 * 60) * 61).toLong()

                state.address = it

                AppManager.instance.wallet?.saveAddress(it.toDTO(), true)

                Handler(Looper.getMainLooper()).postDelayed({
                    updateToken()
                    initViewAddress(state.address)
                }, 100)
            }
        }

        offlineAddressSubscription = AppManager.instance.subOnOfflineAddress.subscribe {
            state.address?.tokenOffline = it
            initViewAddress(state.address)
        }

        maxAddressSubscription = AppManager.instance.subOnMaxPrivacyAddress.subscribe {
            state.address?.tokenMaxPrivacy = it
            initViewAddress(state.address)
        }

        regularAddressSubscription = AppManager.instance.subOnRegularAddress.subscribe {
            state.address = WalletAddress(it)
            initViewAddress(state.address)
            updateToken()
        }

        if (state.address == null) {
            val amount = view?.getAmount()?.convertToGroth() ?: 0L
            val assetId = view?.getAssetId() ?: 0
            AppManager.instance.wallet?.generateRegularAddress(amount, assetId)

//            AppManager.instance.wallet?.generateNewAddress()
        }
    }

    override fun onTokenPressed() {
        if (transaction == TransactionTypeOptions.MAX_PRIVACY) {
            view?.showShowToken(state.address?.tokenMaxPrivacy ?: "")
        }
        else {
            if (!AppManager.instance.isMaxPrivacyEnabled()) {
                view?.showShowToken(state.address?.address ?: "")
            }
            else {
                view?.showShowToken(state.address?.tokenOffline ?: "")
            }
        }
    }

//    private fun saveAddress() {
//        state.address?.let { address ->
//            val comment = view?.getComment()
//            address.label = comment ?: ""
//
//            view?.getTxComment()?.let {
//                if (it.isNotBlank()) {
//                    ReceiveTxCommentHelper.saveCommentToAddress(address.id, it)
//                }
//            }
//
//           saveToken()
//        }
//    }

    override fun onMaxPrivacyPressed() {
        transaction = TransactionTypeOptions.MAX_PRIVACY
        initViewAddress(state.address)
    }

    override fun onRegularPressed() {
        transaction = TransactionTypeOptions.REGULAR
        initViewAddress(state.address)
    }

    override fun updateToken() {
        requestAddresses()
    }

    private fun requestAddresses() {
        if(state.address != null) {
            val amount = view?.getAmount()?.convertToGroth() ?: 0L
            val assetId = view?.getAssetId() ?: 0

            val isNew = (oldAmount != amount || oldAssetId != assetId || forceRequest)
            oldAmount = amount
            oldAssetId = assetId

            if (!isSBBS) {
                if (state.address?.tokenOffline.isNullOrEmpty() || isNew) {
                    AppManager.instance.wallet?.generateOfflineAddress(oldAmount, assetId)
                }

                if (state.address?.tokenMaxPrivacy.isNullOrEmpty() || isNew) {
                    AppManager.instance.wallet?.generateMaxPrivacyAddress(oldAmount, assetId)
                }
            }

            view?.updateTokens(state.address!!, transaction)

            forceRequest = false
        }
    }

    override fun hasStatus(): Boolean = true

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletIdSubscription, maxAddressSubscription, regularAddressSubscription, offlineAddressSubscription)
}
