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

package com.mw.beam.beamwallet.screens.send

import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.subscribeIf
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendPresenter(currentView: SendContract.View, currentRepository: SendContract.Repository, private val state: SendState)
    : BasePresenter<SendContract.View, SendContract.Repository>(currentView, currentRepository),
        SendContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var addressesSubscription: Disposable
    private lateinit var walletIdSubscription: Disposable
    private lateinit var trashSubscription: Disposable
    private val changeAddressLiveData = MutableLiveData<WalletAddress>()

    companion object {
        const val FORK_MIN_FEE = 100
        const val MAX_FEE = 1000
        private const val DEFAULT_FEE = 10
        private const val MAX_FEE_LENGTH = 15
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(DEFAULT_FEE, MAX_FEE)
        state.privacyMode = repository.isPrivacyModeEnabled()
        state.prevFee = DEFAULT_FEE.toLong()

        val address: String? = view?.getAddressFromArguments()
        if (!address.isNullOrBlank()) {
            view?.setAddress(address)
            onTokenChanged(address)
            view?.setAmount(view?.getAmountFromArguments()?.convertToBeam() ?: 0.0)
        }

        changeAddressLiveData.observe(view!!.getLifecycleOwner(), Observer {
            if (it.walletID != state.outgoingAddress?.walletID) {
                state.isNeedGenerateNewAddress = false
                state.wasAddressSaved = state.generatedAddress?.walletID != it.walletID
                setAddress(it, !state.wasAddressSaved)
            }
        })
    }

    override fun onAddressChanged(walletAddress: WalletAddress) {
        changeAddressLiveData.postValue(walletAddress)
    }

    override fun onLabelAddressChanged(text: String) {
        if (!state.wasAddressSaved) {
            state.outgoingAddress?.label = text
        }
    }

    override fun onStart() {
        super.onStart()

        // we need to apply scanned address after watchers were added
        if (state.scannedAddress != null) {
            state.scannedAddress?.let {
                view?.setAddress(it)
                view?.handleAddressSuggestions(null)
                view?.requestFocusToAmount()
            }
            state.scannedAmount?.let { view?.setAmount(it) }

            state.scannedAddress = null
            state.scannedAmount = null
        }

        view?.handleExpandAdvanced(state.expandAdvanced)
        view?.handleExpandEditAddress(state.expandEditAddress)

        view?.updateFeeViews(false)

        onTokenChanged(view?.getToken(), searchAddress = false)

        state.outgoingAddress?.let { setAddress(it, !state.wasAddressSaved) }

        notifyPrivacyStateChange()
    }

    override fun onSelectAddress(walletAddress: WalletAddress) {
        view?.setAddress(walletAddress.walletID)
        view?.handleAddressSuggestions(null)
        view?.requestFocusToAmount()
    }

    private fun notifyPrivacyStateChange() {
        val privacyModeEnabled = repository.isPrivacyModeEnabled()
        state.privacyMode = privacyModeEnabled
        view?.configPrivacyStatus(privacyModeEnabled)
    }

    override fun onChangePrivacyModePressed() {
        if (!state.privacyMode && repository.isNeedConfirmEnablePrivacyMode()) {
            view?.showActivatePrivacyModeDialog()
        } else {
            repository.setPrivacyModeEnabled(!state.privacyMode)
            notifyPrivacyStateChange()
        }
    }

    override fun onSendAllPressed() {
        val availableAmount = state.walletStatus!!.available
        val feeAmount = try {
            view?.getFee() ?: 0L
        } catch (exception: NumberFormatException) {
            0L
        }

        setAmount(availableAmount, feeAmount)
        view?.hasAmountError(view?.getAmount()?.convertToGroth()
                ?: 0, feeAmount, state.walletStatus!!.available, state.privacyMode)
        if (availableAmount <= feeAmount) {
            view?.setAmount(availableAmount.convertToBeam())
            onAmountUnfocused()
        }
        view?.updateFeeTransactionVisibility(true)
    }

    override fun onPaste() {
        state.isPastedText = true
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        repository.setPrivacyModeEnabled(true)
        notifyPrivacyStateChange()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        view?.createOptionsMenu(menu, inflater, state.privacyMode)
    }

    override fun onAddNewCategoryPressed() {
        view?.showAddNewCategory()
    }

    override fun onNext() {
        if (view?.hasErrors(state.walletStatus?.available ?: 0, state.privacyMode) == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()
            val comment = view?.getComment()
            val token = view?.getToken()

            if (amount != null && fee != null && token != null && isValidToken(token)) {

                if (isFork() && fee < FORK_MIN_FEE) {
                    view?.showMinFeeError()
                    return
                }

                // we can't send money to own expired address
                if (state.addresses.values.find { it.walletID == token && it.isExpired && !it.isContact } != null) {
                    view?.showCantSendToExpiredError()
                } else if (state.outgoingAddress != null) {
                    saveAddress()
                    view?.showConfirmTransaction(state.outgoingAddress!!.walletID, token, comment, amount.convertToGroth(), fee)
                }
            }
        }
    }

    private fun isValidToken(token: String): Boolean {
        return QrHelper.isValidAddress(token)
    }

    override fun onEnterFee(rawFee: String?) {
        rawFee?.let {
            view?.setFee(it)
            onFeeChanged(it)
        }
    }

    override fun onLongPressFee() {
        view?.showFeeDialog()
    }

    override fun onAdvancedPressed() {
        state.expandAdvanced = !state.expandAdvanced
        if (!state.expandAdvanced) {
            state.expandEditAddress = false
            view?.handleExpandEditAddress(state.expandEditAddress)
        }

        view?.handleExpandAdvanced(state.expandAdvanced)
    }

    override fun onEditAddressPressed() {
        state.expandEditAddress = !state.expandEditAddress
        view?.handleExpandEditAddress(state.expandEditAddress)
    }

    override fun onChangeAddressPressed() {
        view?.showChangeAddressFragment(state.generatedAddress)
    }

    override fun onExpirePeriodChanged(period: ExpirePeriod) {
        state.expirePeriod = period
        state.outgoingAddress?.duration = period.value
    }

    override fun onSelectedCategory(category: Category?) {
        state.outgoingAddress?.let { repository.changeCategoryForAddress(it.walletID, category) }
    }

    private fun saveAddress() {
        if (state.outgoingAddress != null) {
            state.outgoingAddress!!.duration = state.expirePeriod.value

            val comment = view?.getCommentOutgoingAddress()

            state.outgoingAddress!!.label = comment ?: ""

            if (state.wasAddressSaved) {
                repository.updateAddress(state.outgoingAddress!!)
            } else {
                repository.saveAddress(state.outgoingAddress!!)
            }

            state.wasAddressSaved = true
            state.isNeedGenerateNewAddress = false
        }
    }

    override fun onScanQrPressed() {
        if (view?.isPermissionGranted() == true) {
            view?.scanQR()
        }
    }

    override fun onScannedQR(text: String?) {
        if (text == null) return

        val scannedAddress = QrHelper.getScannedAddress(text)
        val isValidAddress = QrHelper.isValidAddress(scannedAddress)

        if (isValidAddress) {
            state.scannedAddress = scannedAddress

            if (QrHelper.isNewQrVersion(text)) {
                val qrObject = QrHelper.parseQrCode(text)

                state.scannedAmount = qrObject.amount
            }

            view?.requestFocusToAmount()
        } else {
            view?.showNotBeamAddressError()
        }
    }

    override fun onRequestPermissionsResult(result: PermissionStatus) {
        when (result) {
            PermissionStatus.GRANTED -> view?.scanQR()
            PermissionStatus.NEVER_ASK_AGAIN -> {
                view?.showPermissionRequiredAlert()
            }
            PermissionStatus.DECLINED -> {
                //do nothing
            }
        }
    }

    override fun onTokenChanged(rawToken: String?, searchAddress: Boolean) {
        val validToken = isValidToken(rawToken ?: "")
        view?.changeTokenColor(validToken)

        view?.clearAddressError()
        if (rawToken != null && isValidToken(rawToken)) {
            val address = state.addresses.values.firstOrNull { it.walletID == rawToken }
            val category = address?.let { repository.getCategory(it.walletID) }
            view?.setSendContact(address, category)
        } else {
            view?.setSendContact(null, null)
        }

        val isPastedToken = state.isPastedText && validToken

        if (searchAddress && !isPastedToken) {
            updateSuggestions(rawToken, true)
        } else if (isPastedToken) {
            view?.handleAddressSuggestions(null)
            view?.requestFocusToAmount()
        }

        state.isPastedText = false
    }

    private fun updateSuggestions(rawToken: String?, changeSuggestionsVisibility: Boolean) {
        val addresses = if (!rawToken.isNullOrBlank()) {
            val searchText = rawToken.trim().toLowerCase()
            state.addresses.values.filter {
                (!it.isExpired || it.isContact) && (it.walletID.trim().toLowerCase().startsWith(searchText) ||
                        it.label.trim().toLowerCase().contains(searchText) ||
                        repository.getCategory(it.walletID)?.name?.trim()?.toLowerCase()?.contains(searchText) ?: false)
            }
        } else {
            state.addresses.values.filter { !it.isExpired || it.isContact }
        }

        view?.handleAddressSuggestions(addresses, changeSuggestionsVisibility)
    }

    override fun onAmountChanged() {
        view?.apply {
            clearErrors()
            updateFeeTransactionVisibility(false)
        }
    }

    override fun onAmountUnfocused() {
        view?.apply {
            val amount = getAmount()
            val fee = getFee()


            hasAmountError(amount.convertToGroth(), fee, state.walletStatus?.available
                    ?: 0, state.privacyMode)
        }
    }

    override fun onFeeChanged(rawFee: String?) {
        if (rawFee != null && rawFee.length > MAX_FEE_LENGTH) {
            view?.setFee(rawFee.substring(0, MAX_FEE_LENGTH))
        }
        val feeAmount = try {
            view?.getFee() ?: 0L
        } catch (exception: NumberFormatException) {
            0L
        }

        view?.clearErrors()

        val enteredAmount = view?.getAmount()?.convertToGroth() ?: 0L
        val availableAmount = state.walletStatus!!.available
        val maxEnterAmount = availableAmount - feeAmount
        when {
            enteredAmount > maxEnterAmount || enteredAmount.convertToBeamString() == (availableAmount - state.prevFee).convertToBeamString() -> {
                setAmount(availableAmount, feeAmount)
                view?.hasAmountError(maxEnterAmount, feeAmount, state.walletStatus!!.available, state.privacyMode)
                view?.updateFeeTransactionVisibility(true)
            }
            else -> view?.updateFeeTransactionVisibility(false)
        }

        state.prevFee = feeAmount
    }

    private fun setAmount(availableAmount: Long, fee: Long) {
        val maxEnterAmount = availableAmount - fee
        if (maxEnterAmount > 0) {
            view?.setAmount(maxEnterAmount.convertToBeam())
        }
    }

    private fun isFork() = state.walletStatus?.system?.height ?: 0 >= AppConfig.FORK_HEIGHT

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
            view?.updateAvailable(state.walletStatus!!.available)
            if (isFork()) {
                view?.setupMinFee(FORK_MIN_FEE)
            }
        }

        addressesSubscription = repository.getAddresses().subscribe {
            it.addresses?.forEach { address ->
                state.addresses[address.walletID] = address
            }

            repository.getAllAddressesInTrash().forEach { address ->
                state.addresses.remove(address.walletID)
            }
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    it.data.addresses.forEach { address ->
                        state.addresses.remove(address.walletID)
                    }
                    updateSuggestions(view?.getToken(), false)
                }
                TrashManager.ActionType.Restored -> {
                    it.data.addresses.forEach { address ->
                        state.addresses[address.walletID] = address
                    }
                    updateSuggestions(view?.getToken(), false)
                }
                TrashManager.ActionType.Removed -> {
                }
            }
        }

        walletIdSubscription = repository.generateNewAddress().subscribeIf(state.isNeedGenerateNewAddress) {
            state.generatedAddress = it
            setAddress(it, true)
            state.isNeedGenerateNewAddress = false
        }
    }

    private fun setAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        state.outgoingAddress = walletAddress
        view?.configOutgoingAddress(walletAddress, isGenerated)
        view?.configCategory(repository.getCategory(walletAddress.walletID))

    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, addressesSubscription, walletIdSubscription, trashSubscription)

    override fun hasStatus(): Boolean = true
}
