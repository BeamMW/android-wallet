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

package com.mw.beam.beamwallet.screens.unlink

import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.*

/**
 *  11/13/18.
 */
class UnlinkPresenter(currentView: UnlinkContract.View, currentRepository: UnlinkContract.Repository, private val state: UnlinkState)
    : BasePresenter<UnlinkContract.View, UnlinkContract.Repository>(currentView, currentRepository),
        UnlinkContract.Presenter {

    val FORK_MIN_FEE = 1100
    var MAX_FEE = 2000
    val DEFAULT_FEE = 1100
    val MAX_FEE_LENGTH = 15

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init(DEFAULT_FEE, MAX_FEE)

        state.privacyMode = repository.isPrivacyModeEnabled()
        state.prevFee = DEFAULT_FEE.toLong()
    }


    override fun onStart() {
        super.onStart()

        view?.updateFeeViews(false)

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
        val availableAmount = state.walletStatus!!.unlinked
        val feeAmount = try {
            view?.getFee() ?: 0L
        } catch (exception: NumberFormatException) {
            0L
        }

        setAmount(availableAmount, feeAmount)
        view?.hasAmountError(view?.getAmount()?.convertToGroth()
                ?: 0, feeAmount, state.walletStatus!!.unlinked, state.privacyMode)
        if (availableAmount <= feeAmount) {
            view?.setAmount(availableAmount.convertToBeam())
            onAmountUnfocused()
        }
        view?.updateFeeTransactionVisibility()
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
        if (view?.hasErrors(state.walletStatus?.unlinked ?: 0, state.privacyMode) == false) {
            val amount = view?.getAmount()
            val fee = view?.getFee()

            if (amount != null && fee != null) {

                if (isFork() && fee < FORK_MIN_FEE) {
                    view?.showMinFeeError()
                    return
                }

                view?.showConfirmTransaction(amount.convertToGroth(), fee)
            }
        }
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

    override fun onAmountChanged() {
        view?.apply {
            clearErrors()
            updateFeeTransactionVisibility()
        }
    }

    override fun onAmountUnfocused() {
        view?.apply {
            val amount = getAmount()
            val fee = getFee()

            hasAmountError(amount.convertToGroth(), fee, state.walletStatus?.unlinked
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
        val availableAmount = state.walletStatus!!.unlinked
        val maxEnterAmount = availableAmount - feeAmount
        when {
            enteredAmount > maxEnterAmount || enteredAmount.convertToBeamString() == (availableAmount - state.prevFee).convertToBeamString() -> {
                setAmount(availableAmount, feeAmount)
                view?.hasAmountError(maxEnterAmount, feeAmount, state.walletStatus!!.unlinked, state.privacyMode)
                view?.updateFeeTransactionVisibility()
            }
            else -> view?.updateFeeTransactionVisibility()
        }

        state.prevFee = feeAmount
    }

    private fun setAmount(availableAmount: Long, fee: Long) {
        val maxEnterAmount = availableAmount - fee
        if (maxEnterAmount > 0) {
            view?.setAmount(maxEnterAmount.convertToBeam())
        }
    }

    private fun isFork() = true

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.walletStatus = AppManager.instance.getStatus()
        view?.updateAvailable(state.walletStatus!!.unlinked)
        if (isFork()) {
            view?.setupMinFee(FORK_MIN_FEE)
        }
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun hasStatus(): Boolean = true
}
