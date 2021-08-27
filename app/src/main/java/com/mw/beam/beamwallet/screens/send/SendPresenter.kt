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

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.core.utils.subscribeIf
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable

/**
 *  11/13/18.
 */
class SendPresenter(currentView: SendContract.View, currentRepository: SendContract.Repository, val state: SendState)
    : BasePresenter<SendContract.View, SendContract.Repository>(currentView, currentRepository),
        SendContract.Presenter {

    var FORK_MIN_FEE = 100
    var MAX_FEE = 2000
    var DEFAULT_FEE = 100
    val MAX_FEE_LENGTH = 15
    var change = 0L
    var inputShield = 0L
    var isAllPressed = false
    var assetId = -1
    var maxSelected = 0L
    var maxAvailableAmount = ""

    private lateinit var walletStatusSubscription: Disposable
    private lateinit var addressesSubscription: Disposable
    private lateinit var walletIdSubscription: Disposable
    private var offlineCountSubscription: Disposable? = null
    private lateinit var feeSubscription: Disposable

    private val changeAddressLiveData = MutableLiveData<WalletAddress>()

    override fun onViewCreated() {
        super.onViewCreated()

        if (assetId == -1) {
            assetId = AssetManager.instance.selectedAssetId
        }

        view?.init(DEFAULT_FEE, MAX_FEE)
        state.privacyMode = repository.isPrivacyModeEnabled()
        state.prevFee = DEFAULT_FEE.toLong()

        val address: String? = view?.getAddressFromArguments()

        if (!address.isNullOrBlank()) {
            val amount = view?.getAmountFromArguments()?.convertToBeam() ?: 0.0
            view?.setAddress(address)
            onTokenChanged(address)

            if (amount>0) {
                view?.setAmount(amount)
                view?.hideKeyboard()

                val availableAmount = AssetManager.instance.getAvailable(assetId)
                val feeAmount = try {
                    view?.getFee() ?: 0L
                } catch (exception: NumberFormatException) {
                    0L
                }

                view?.hasAmountError(view?.getAmount()?.convertToGroth()
                        ?: 0, feeAmount, availableAmount, state.privacyMode)
            }

            onScannedQR(view?.getToken(), false)
        }
        else if (!view?.getToken().isNullOrEmpty()) {
            onScannedQR(view?.getToken(), false)
        }


        changeAddressLiveData.observe(view!!.getLifecycleOwner(), Observer {
            if (it.id != state.outgoingAddress?.id) {
                state.isNeedGenerateNewAddress = false
                state.wasAddressSaved = state.generatedAddress?.id != it.id
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
              //  view?.requestFocusToAmount()
            }
            state.scannedAmount?.let { view?.setAmount(it) }

            state.scannedAddress = null
            state.scannedAmount = null
        }

        view?.handleExpandAdvanced(state.expandAdvanced)
        view?.handleExpandEditAddress(state.expandEditAddress)

        view?.updateFeeViews(false)

        onTokenChanged(view?.getToken(), searchAddress = false)

        state.outgoingAddress?.let {
            setAddress(it, !state.wasAddressSaved)
        }

        notifyPrivacyStateChange()

        requestFee()
    }

    override fun onMaxPrivacy(value: Boolean) {
        val old = DEFAULT_FEE

        if(value) {
            MAX_FEE = 12000000
            FORK_MIN_FEE = 1000100
            DEFAULT_FEE = FORK_MIN_FEE
        }
        else {
            FORK_MIN_FEE = 100
            MAX_FEE = 2000
            DEFAULT_FEE = FORK_MIN_FEE
        }

        if (old != DEFAULT_FEE) {
            view?.setupMaxFee(MAX_FEE, FORK_MIN_FEE)
            onEnterFee(DEFAULT_FEE.toString())
        }

        requestFee()
    }

    override fun requestFee() {
        Log.e("FEE", "REQUEST FEE")
        val enteredAmount = view?.getAmount()?.convertToGroth() ?: 0L
        val fee = view?.getFee() ?: 0L
        val isShielded = (view?.isOffline() == true || state.addressType == BMAddressType.BMAddressTypeOfflinePublic ||
                state.addressType == BMAddressType.BMAddressTypeMaxPrivacy)
        AppManager.instance.wallet?.selectCoins(enteredAmount + fee, 0L, isShielded, assetId)
    }

    private fun onFeeDidCalculated(fee: Long) {
        if (FORK_MIN_FEE != fee.toInt()) {
            FORK_MIN_FEE = fee.toInt()
            MAX_FEE = if (fee < 300) {
                2000
            } else {
                fee.toInt() * 2
            }
            DEFAULT_FEE = FORK_MIN_FEE

            view?.setupMaxFee(MAX_FEE, FORK_MIN_FEE)
            onEnterFee(DEFAULT_FEE.toString())
        }
    }

    override fun onSelectAddress(walletAddress: WalletAddress) {
        if(AppManager.instance.wallet?.isToken(walletAddress.address) == true) {
            val params = AppManager.instance.wallet?.getTransactionParameters(walletAddress.address, true)
            if((params?.assetId ?:0) != 0) {
                assetId = params?.assetId ?: 0
            }
            state.addressType = params?.getAddressType() ?: BMAddressType.BMAddressTypeRegular
        }
        else {
            state.addressType = BMAddressType.BMAddressTypeRegular
        }
        view?.setAddress(walletAddress.id)
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

    override fun showTokenFragmentPressed() {
        view?.showTokenFragment()
    }

    override fun onSendAllPressed() {
        if (view?.getAmountText() == maxAvailableAmount && !maxAvailableAmount.isEmpty()) {
            return
        }
        isAllPressed = true
        onAmountUnfocused()

        val availableAmount = AssetManager.instance.getAvailable(assetId)

        val feeAmount = try {
            view?.getFee() ?: 0L
        } catch (exception: NumberFormatException) {
            0L
        }

        val isShielded = (view?.isOffline() == true || state.addressType == BMAddressType.BMAddressTypeOfflinePublic ||
                state.addressType == BMAddressType.BMAddressTypeMaxPrivacy)
        AppManager.instance.wallet?.selectCoins(availableAmount + feeAmount, 0L, isShielded, assetId)

//        setAmount(availableAmount, feeAmount)
//
//        view?.hasAmountError(view?.getAmount()?.convertToGroth()
//                ?: 0, feeAmount, availableAmount, state.privacyMode)
//        if (availableAmount <= feeAmount) {
//            view?.setAmount(availableAmount.convertToBeam())
//            onAmountUnfocused()
//        }
//        view?.updateFeeTransactionVisibility()
//
//        maxAvailableAmount = view?.getAmountText() ?: ""
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

    override fun onNext() {
        if (view?.hasErrors(AssetManager.instance.getAvailable(assetId), state.privacyMode) == false) {
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
                if (state.addresses.values.find { it.id == token && it.isExpired && !it.isContact } != null) {
                    view?.showCantSendToExpiredError()
                } else if (state.outgoingAddress != null) {
                    saveAddress()
                    view?.showConfirmTransaction(state.outgoingAddress!!.id, token, comment, amount.convertToGroth(), fee)
                }
            }
        }
    }

    private fun isValidToken(token: String): Boolean {
        return AppManager.instance.isValidAddress(token)
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

    override fun onScannedQR(text: String?, requestFocus: Boolean) {
        if (text == null) return

        val scannedAddress = QrHelper.getScannedAddress(text)
        val isValidAddress = AppManager.instance.isValidAddress(scannedAddress)
        val old = state.scannedAddress

        if (isValidAddress) {
            state.scannedAddress = scannedAddress

            if (QrHelper.isNewQrVersion(text)) {
                val qrObject = QrHelper.parseQrCode(text)

                state.scannedAmount = qrObject.amount
            }

            subscribeToOfflineCount()

            if(AppManager.instance.wallet?.isToken(scannedAddress) == true) {
                val params = AppManager.instance.wallet?.getTransactionParameters(scannedAddress, true)
                if(params!=null) {
                    if((params.assetId) != 0) {
                        assetId = params.assetId
                    }
                    state.addressType = params.getAddressType()
                    val isShielded = (state.addressType == BMAddressType.BMAddressTypeShielded || state.addressType == BMAddressType.BMAddressTypeOfflinePublic ||
                            state.addressType == BMAddressType.BMAddressTypeMaxPrivacy)
                    if(isShielded) {
                        onMaxPrivacy(true)
                    }
                    if(params.amount > 0) {
                        state.scannedAmount = params.amount.convertToBeam()
                        state.scannedAmount?.let { view?.setAmount(it) }
                    }
                }
                else {
                    state.addressType = BMAddressType.BMAddressTypeUnknown
                }
            }
            else {
                state.maxPrivacyCount = -1
            }

            val enteredAmount = view?.getAmount()?.convertToGroth() ?: 0L

            if((state.scannedAmount == 0.0 || state.scannedAmount == null) && enteredAmount == 0L) {
              android.os.Handler().postDelayed({
                  if (requestFocus) {
                      view?.requestFocusToAmount()
                  }
                  view?.clearErrors()
                }, 200)
            }
            else if (text.startsWith(QrHelper.BEAM_QR_PREFIX)){
                android.os.Handler().postDelayed({
                    view?.apply {
                        val amount = getAmount()
                        val fee = getFee()

                        hasAmountError(amount.convertToGroth(), fee, AssetManager.instance.getAvailable(assetId)
                                ?: 0, state.privacyMode)
                    }

                    view?.hideKeyboard()

                }, 200)
            }
        } else {
            view?.vibrate(100)
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
     //   view?.changeTokenColor(validToken)

        view?.clearAddressError()
        if (rawToken != null && isValidToken(rawToken)) {
            val address = state.addresses.values.firstOrNull { it.id == rawToken }
            view?.setSendContact(address)
        } else {
            view?.setSendContact(null)
        }

        val isPastedToken = state.isPastedText && validToken

        if(rawToken == view?.getAddressFromArguments())
        {

        }
        else if (searchAddress && !isPastedToken) {
            updateSuggestions(rawToken, true)
        }
        else if (isPastedToken) {
            view?.handleAddressSuggestions(null)
        //    view?.requestFocusToAmount() TODO: ?? CHECK
        }

        state.isPastedText = false

//        if(state.isMaxPrivacyRequested && !state.scannedAddress.isNullOrEmpty() && rawToken != state.scannedAddress) {
//            state.isMaxPrivacyRequested = false
//            view?.setMaxPrivacyRequested(false)
//        }

        if(!rawToken.isNullOrEmpty() && AppManager.instance.isValidAddress(rawToken)) {
            val params = AppManager.instance.wallet?.getTransactionParameters(rawToken, true)
            if((params?.assetId ?: 0) != 0) {
                assetId = params?.assetId ?: 0
            }
            state.addressType = params?.getAddressType() ?: BMAddressType.BMAddressTypeRegular
        }
    }

    private fun updateSuggestions(rawToken: String?, changeSuggestionsVisibility: Boolean) {
        val addresses = if (!rawToken.isNullOrBlank()) {
            val searchText = rawToken.trim().toLowerCase()
            state.addresses.values.filter {
                (!it.isExpired || it.isContact) && (it.id.trim().toLowerCase().startsWith(searchText) ||
                        it.label.trim().toLowerCase().contains(searchText))
            }
        } else {
            state.addresses.values.filter { !it.isExpired || it.isContact }
        }

        view?.handleAddressSuggestions(addresses, changeSuggestionsVisibility)
    }

    override fun onAmountChanged() {
        Log.e("FEE", "ON AMOUNT CHANGE FEE")

        if (view?.isIgnoreAmountWatcher() == false) {
            val amount = view?.getAmount()
            //
            if(amount != null && amount > 0) {
                view?.apply {
                    updateFeeTransactionVisibility()
                }
                requestFee()

                if (amount != null) {
                    if(amount <= 0.0) {
                        view?.clearErrors()
                    }
                }
                else {
                    view?.clearErrors()
                }
            }
            else {
                view?.clearErrors()
            }
        }
    }

    override fun onAmountUnfocused() {
        view?.apply {
            val amountText = getAmountText()
            if (amountText.isNotEmpty()) {
                val amount = getAmount()
                val fee = getFee()

                hasAmountError(amount.convertToGroth(), fee, AssetManager.instance.getAvailable(assetId)
                        ?: 0, state.privacyMode)
            }
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

        var enteredAmount = view?.getAmount()?.convertToGroth() ?: 0L
        val availableAmount = AssetManager.instance.getAvailable(assetId)
        val maxEnterAmount = availableAmount - feeAmount
        when {
            enteredAmount > maxEnterAmount || enteredAmount.convertToBeamString() == (availableAmount - state.prevFee).convertToBeamString() -> {
                if(isAllPressed) {
                    enteredAmount = availableAmount - feeAmount
                    setAmount(availableAmount, feeAmount)
                }
                view?.hasAmountError(enteredAmount, feeAmount,  AssetManager.instance.getAvailable(assetId), state.privacyMode)
                view?.updateFeeTransactionVisibility()
            }
            else -> view?.updateFeeTransactionVisibility()
        }

        state.prevFee = feeAmount
    }

    private fun setAmount(availableAmount: Long, fee: Long) {
        val maxEnterAmount = if(assetId != 0) {
            availableAmount
        }
        else {
            availableAmount - fee
        }
        if (maxEnterAmount > 0) {
            view?.setAmount(maxEnterAmount.convertToBeam())
        }
    }

    private fun isFork() = true
    //state.walletStatus?.system?.height ?: 0 >= AppConfig.FORK_HEIGHT

    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.updateAvailable( AssetManager.instance.getAvailable(assetId))

        if (isFork()) {
            view?.setupMinFee(FORK_MIN_FEE)
        }

        feeSubscription = WalletListener.subOnFeeCalculated.subscribe {
            AppActivity.self.runOnUiThread {

                inputShield = it.shieldedInputsFee
                change = it.change
                onFeeDidCalculated(it.fee)

                if(isAllPressed) {
                    maxSelected = it.max - it.fee

                    if (view?.getAmount()?.convertToGroth() != it.max - it.fee) {
                        setAmount(it.max, it.fee)

                        val availableAmount = AssetManager.instance.getAvailable(assetId)
                        val error = view?.hasAmountError(maxSelected, it.fee, availableAmount, state.privacyMode)
                        if (!error!!) {
                            view?.clearErrors()
                        }

                        maxAvailableAmount = view?.getAmountText() ?: ""
                    }
                }
                else {
                    val amount = view?.getAmount()
                    if (amount != null) {
                        if(amount > 0.0) {
                            maxSelected = it.max - it.fee

                            val availableAmount = AssetManager.instance.getAvailable(assetId)
                            val feeAmount = try {
                                view?.getFee() ?: 0L
                            } catch (exception: NumberFormatException) {
                                0L
                            }

                            val error = view?.hasAmountError(amount.convertToGroth(), feeAmount, availableAmount, state.privacyMode)
                            if (!error!!) {
                                view?.clearErrors()
                            }
                        }
                        else {
                            view?.clearErrors()
                        }
                    }
                    else {
                        view?.clearErrors()
                    }
                }

                view?.onCalcFeeDone()
            }
        }

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe(){
            view?.updateAvailable( AssetManager.instance.getAvailable(assetId))
        }

        AppManager.instance.getAllAddresses().forEach { address ->
            state.addresses[address.id] = address
        }

        addressesSubscription = AppManager.instance.subOnAddressesChanged.subscribe(){
           AppManager.instance.getAllAddresses().forEach { address ->
                state.addresses[address.id] = address
            }
        }

        subscribeToOfflineCount()

        walletIdSubscription = AppManager.instance.subOnAddressCreated.subscribe {
            state.generatedAddress = it
            setAddress(it, true)
            state.isNeedGenerateNewAddress = false
        }

        if (state.isNeedGenerateNewAddress) {
            repository.generateNewAddress()
        }
    }

    private fun subscribeToOfflineCount() {
        if(offlineCountSubscription == null || offlineCountSubscription?.isDisposed == true) {
            offlineCountSubscription = AppManager.instance.subOnGetOfflinePayments.subscribe(){
                if(it!=null) {
                    state.maxPrivacyCount = it
                }
                else {
                    state.maxPrivacyCount = -1
                }
                view?.updateMaxPrivacyCount(state.maxPrivacyCount)
            }
        }
    }

    private fun setAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        state.outgoingAddress = walletAddress

        view?.configOutgoingAddress(walletAddress, isGenerated)
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletStatusSubscription, addressesSubscription, walletIdSubscription, offlineCountSubscription!!, feeSubscription)

    override fun hasStatus(): Boolean = true
}
