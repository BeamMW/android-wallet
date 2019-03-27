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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import com.mw.beam.beamwallet.core.helpers.convertToGroth
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
    private val tokenRegex = Regex("[^A-Fa-f0-9]")
    private val defaultFee = 10
    private val zeroFee = 0

    companion object {
        private const val MAX_TOKEN_LENGTH = 80
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(defaultFee)
    }

    override fun onStart() {
        super.onStart()

        // we need to apply scanned address after watchers were added
        if (state.scannedAddress != null) {
            view?.setAddress(state.scannedAddress!!)
            state.scannedAddress = null
        }
    }

    override fun onSend() {
        if (view?.hasErrors(state.walletStatus?.available ?: 0) == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()
            val comment = view?.getComment()
            val token = view?.getToken()

            if (amount != null && fee != null && token != null && state.isTokenValid) {
                // we can't send money to own expired address
                if (state.expiredAddresses.find { it.walletID == token } != null) {
                    view?.showCantSendToExpiredError()
                } else {
                    repository.sendMoney(token, comment, amount.convertToGroth(), fee)
                    view?.close()
                }
            }
        }
    }

    override fun onFeeFocusChanged(isFocused: Boolean, fee: String) {
        if (!isFocused) {
            val feeAmount = try {
                fee.toInt()
            } catch (exception: NumberFormatException) {
                0
            }

            //to prevent multizero input
            view?.setFee(feeAmount)
        }
    }

    override fun onScanQrPressed() {
        if (view?.isPermissionGranted() == true) {
            view?.scanQR()
        }
    }

    override fun onScannedQR(address: String?) {
        val clearedAddress = address?.replace(tokenRegex, "")

        when {
            address != null && address != clearedAddress -> view?.showNotBeamAddressError()
            address != null && address == clearedAddress -> state.scannedAddress = address
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

    override fun onTokenPasted(token: String?, oldToken: String?) {
        state.oldToken = oldToken
        state.isChangeForbidden = token?.replace(tokenRegex, "") != token

        if (token?.replace(tokenRegex, "") != token) {
            state.isChangeForbidden = true
            view?.showCantPasteError()
        }
    }

    override fun onTokenChanged(rawToken: String?) {
        if (state.isChangeForbidden) {
            state.isChangeForbidden = false
            view?.clearToken(state.oldToken)
        } else {
            state.isChangeForbidden = false
            var clearedToken = rawToken?.replace(tokenRegex, "")

            if (!clearedToken.isNullOrEmpty() && clearedToken.length > MAX_TOKEN_LENGTH) {
                clearedToken = clearedToken.substring(0, MAX_TOKEN_LENGTH)
            }

            if (rawToken == clearedToken) {
                val isTokenEmpty = rawToken.isNullOrEmpty()

                if (isTokenEmpty != state.isTokenEmpty) {
                    view?.updateUI(!isTokenEmpty, defaultFee)
                }

                if (!isTokenEmpty) {
                    if (repository.checkAddress(rawToken)) {
                        view?.clearAddressError()
                        state.isTokenValid = true
                    } else {
                        view?.setAddressError()
                        state.isTokenValid = false
                    }
                } else {
                    state.isTokenValid = false
                }

                state.isTokenEmpty = isTokenEmpty
            } else {
                view?.clearToken(clearedToken)
            }
        }
    }

    override fun onAmountChanged() {
        view?.clearErrors()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
        }

        cantSendToExpiredSubscription = repository.onCantSendToExpired().subscribe {
            // just for case when address was expired directly before sendMoney() was called
            view?.close()
        }

        addressesSubscription = repository.getAddresses().subscribe {
            state.expiredAddresses = it.addresses?.filter { address -> address.isExpired }
                    ?: listOf()
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, cantSendToExpiredSubscription, addressesSubscription)

    override fun hasStatus(): Boolean = true
}
