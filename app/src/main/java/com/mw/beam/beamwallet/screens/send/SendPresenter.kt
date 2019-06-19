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
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendPresenter(currentView: SendContract.View, currentRepository: SendContract.Repository, private val state: SendState)
    : BasePresenter<SendContract.View, SendContract.Repository>(currentView, currentRepository),
        SendContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var cantSendToExpiredSubscription: Disposable
    private lateinit var addressesSubscription: Disposable
    private lateinit var walletIdSubscription: Disposable
    private val changeAddressLiveData = MutableLiveData<WalletAddress>()

    companion object {
        private const val DEFAULT_FEE = 10
        private const val MAX_FEE = 1000
        private const val ZERO_FEE = 0
        private const val MAX_FEE_LENGTH = 15
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(DEFAULT_FEE, MAX_FEE)
        state.privacyMode = repository.isPrivacyModeEnabled()

        val address: String? = view?.getAddressFromArguments()
        if (!address.isNullOrBlank()) {
            view?.setAddress(address)
            onTokenChanged(address)
            view?.setAmount(view?.getAmountFromArguments()?.convertToBeam() ?: 0.0)
        }

        changeAddressLiveData.observe(view!!.getLifecycleOwner(), Observer {
            setAddress(it, false)
        })
    }

    override fun onAddressChanged(walletAddress: WalletAddress) {
        state.isNeedGenerateNewAddress = false
        changeAddressLiveData.postValue(walletAddress)
    }

    override fun onStart() {
        super.onStart()

        // we need to apply scanned address after watchers were added
        if (state.scannedAddress != null) {
            state.scannedAddress?.let { view?.setAddress(it) }
            state.scannedAmount?.let { view?.setAmount(it) }

            state.scannedAddress = null
            state.scannedAmount = null
        }

        view?.handleExpandAdvanced(state.expandAdvanced)
        view?.handleExpandEditAddress(state.expandEditAddress)

        view?.updateFeeViews()

        notifyPrivacyStateChange()
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
        val availableAmount = state.walletStatus!!.available.convertToBeam()
        val feeAmount = try {
            view?.getFee()?.convertToBeam() ?: 0.0
        } catch (exception: NumberFormatException) {
            0.0
        }

        view?.setAmount(availableAmount - feeAmount)
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

    override fun onSend() {
        if (view?.hasErrors(state.walletStatus?.available ?: 0, state.privacyMode) == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()
            val comment = view?.getComment()
            val token = view?.getToken()

            if (amount != null && fee != null && token != null && isValidToken(token)) {
                // we can't send money to own expired address
                if (state.expiredAddresses.find { it.walletID == token } != null) {
                    view?.showCantSendToExpiredError()
                } else if (state.outgoingAddress != null) {
                    saveAddress()
                    view?.pendingSendMoney(state.outgoingAddress!!.walletID, token, comment, amount.convertToGroth(), fee)
                    view?.close()
                }
            }
        }
    }

    private fun isValidToken(token: String): Boolean {
        return QrHelper.isValidAddress(token)
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
        view?.showChangeAddressFragment()
    }

    override fun onExpirePeriodChanged(period: ExpirePeriod) {
        state.expirePeriod = period
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
        }
    }

    override fun onConfirm() {
        onSend()
        return

//        if (!repository.isConfirmTransactionEnabled()) {
//            return
//        }
//
//        if (view?.hasErrors(state.walletStatus?.available ?: 0, state.privacyMode) == false) {
//            val amount = view?.getAmount()
//            val fee = view?.getFee()
//            val token = view?.getToken()
//
//            if (amount != null && fee != null && token != null && isValidToken(token)) {
//                view?.showConfirmDialog(token, amount, fee)
//            }
//        }
    }

    override fun onFeeFocusChanged(isFocused: Boolean, fee: String) {
        if (!isFocused) {
            val feeAmount = try {
                fee.toInt()
            } catch (exception: NumberFormatException) {
                ZERO_FEE
            }

            //to prevent multizero input
            view?.setFee(feeAmount.toString())
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

    override fun onTokenChanged(rawToken: String?) {
        view?.clearAddressError()


//        if (state.isChangeForbidden) {
//            state.isChangeForbidden = false
//            view?.clearToken(state.oldToken)
//        } else {
//            state.isChangeForbidden = false
//            var clearedToken = rawToken?.replace(QrHelper.tokenRegex, "")
//
//            if (!clearedToken.isNullOrEmpty() && clearedToken.length > MAX_TOKEN_LENGTH) {
//                clearedToken = clearedToken.substring(0, MAX_TOKEN_LENGTH)
//            }
//
//            if (rawToken == clearedToken) {
//                val isTokenEmpty = rawToken.isNullOrEmpty()
//
//                if (isTokenEmpty != state.isTokenEmpty) {
//                    view?.updateUI(DEFAULT_FEE, state.privacyMode)
//                }
//
//                if (!isTokenEmpty) {
//                    if (repository.checkAddress(rawToken)) {
//                        view?.clearAddressError()
//                        state.isTokenValid = true
//                    } else {
//                        view?.setAddressError()
//                        state.isTokenValid = false
//                    }
//                } else {
//                    state.isTokenValid = false
//                }
//
//                state.isTokenEmpty = isTokenEmpty
//            } else {
//                view?.clearToken(clearedToken)
//            }
//        }
    }

    override fun onAmountChanged() {
        view?.clearErrors()
    }

    override fun onFeeChanged(rawFee: String?) {
        if (rawFee != null && rawFee.length > MAX_FEE_LENGTH) {
            view?.setFee(rawFee.substring(0, MAX_FEE_LENGTH))
        }
        val enteredAmount = view?.getAmount() ?: 0.0
        val availableAmount = state.walletStatus!!.available.convertToBeam()
        val feeAmount = try {
            view?.getFee()?.convertToBeam() ?: 0.0
        } catch (exception: NumberFormatException) {
            0.0
        }
        val maxEnterAmount = availableAmount - feeAmount
        if (enteredAmount > maxEnterAmount) {
            view?.setAmount(maxEnterAmount)
        } else if (enteredAmount.convertToBeamString() == (availableAmount - state.prevFee).convertToBeamString()) {
            view?.setAmount(maxEnterAmount)
        }
        state.prevFee = feeAmount
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
            view?.updateAvailable(state.walletStatus!!.available.convertToBeamString())

            if (view?.isAmountErrorShown() == true) {
                view?.hasErrors(state.walletStatus?.available ?: 0, state.privacyMode)
            }
        }

        cantSendToExpiredSubscription = repository.onCantSendToExpired().subscribe {
            // just for case when address was expired directly before sendMoney() was called
            view?.close()
        }

        addressesSubscription = repository.getAddresses().subscribe {
            state.expiredAddresses = it.addresses?.filter { address -> address.isContact && address.isExpired }
                    ?: listOf()
        }

        walletIdSubscription = if (state.isNeedGenerateNewAddress) repository.generateNewAddress().subscribe {
            setAddress(it, true)
        } else {
            EmptyDisposable()
        }
    }

    private fun setAddress(walletAddress: WalletAddress, isGenerated: Boolean) {
        state.outgoingAddress = walletAddress
        view?.configOutgoingAddress(walletAddress, isGenerated)
        view?.configCategory(repository.getCategory(walletAddress.walletID), repository.getAllCategory())

    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, cantSendToExpiredSubscription, addressesSubscription)

    override fun hasStatus(): Boolean = true
}
