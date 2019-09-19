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

package com.mw.beam.beamwallet.screens.wallet

import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable


/**
 *  10/1/18.
 */
class WalletPresenter(currentView: WalletContract.View, currentRepository: WalletContract.Repository, private val state: WalletState)
    : BasePresenter<WalletContract.View, WalletContract.Repository>(currentView, currentRepository),
        WalletContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        repository.saveFinishRestoreFlag()

        view?.init()
        state.privacyMode = repository.isPrivacyModeEnabled()
        val txId = repository.getIntentTransactionId()
        if (txId != null) {
            view?.showTransactionDetails(txId)
        } else {
            view?.clearAllNotification()
        }
    }

    override fun onStart() {
        super.onStart()
        notifyPrivacyStateChange()
        view?.addTitleListeners(state.privacyMode)
    }

    private fun notifyPrivacyStateChange() {
        val privacyModeEnabled = repository.isPrivacyModeEnabled()
        state.privacyMode = privacyModeEnabled
        view?.configPrivacyStatus(privacyModeEnabled)

        if (state.privacyMode) {
            state.prevAvailableExpandState = state.shouldExpandAvailable
            state.prevProgressExpandState = state.shouldExpandInProgress

            state.shouldExpandAvailable = state.privacyMode
            state.shouldExpandInProgress = state.privacyMode

            view?.handleExpandAvailable(state.shouldExpandAvailable)
            view?.handleExpandInProgress(state.shouldExpandInProgress)
        }
        else{
            state.shouldExpandAvailable = state.prevAvailableExpandState
            state.shouldExpandInProgress = state.prevProgressExpandState

            view?.handleExpandAvailable(state.prevAvailableExpandState)
            view?.handleExpandInProgress(state.prevProgressExpandState)
        }


        state.walletStatus?.let { view?.configWalletStatus(it, !state.shouldExpandAvailable, !state.shouldExpandInProgress, privacyModeEnabled) }
    }

    override fun onChangePrivacyModePressed() {
        if (!state.privacyMode && repository.isNeedConfirmEnablePrivacyMode()) {
            view?.showActivatePrivacyModeDialog()
        } else {
            repository.setPrivacyModeEnabled(!state.privacyMode)
            notifyPrivacyStateChange()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        view?.createOptionsMenu(menu, inflater, state.privacyMode)
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        repository.setPrivacyModeEnabled(true)
        notifyPrivacyStateChange()
    }

    override fun onReceivePressed() {
        view?.showReceiveScreen()
    }

    override fun onSendPressed() {
        view?.showSendScreen()
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

    override fun onShowAllPressed() {
        view?.showAllTransactions()
    }

    override fun onCheckShouldExpandAvailable() {
        if (state.shouldExpandAvailable && !state.privacyMode) {
            onExpandAvailablePressed()
        }
    }

    override fun onCheckShouldExpandInProgress() {
        if (state.shouldExpandInProgress && !state.privacyMode) {
            onExpandInProgressPressed()
        }
    }

    override fun onExpandAvailablePressed() {
        if (state.privacyMode) {
            return
        }

        state.shouldExpandAvailable = !state.shouldExpandAvailable
        view?.handleExpandAvailable(state.shouldExpandAvailable)

        view?.configAvailable(state.walletStatus?.available ?: 0, state.walletStatus?.maturing
                ?: 0, !state.shouldExpandAvailable, state.privacyMode)
    }

    override fun onExpandInProgressPressed() {
        if (state.privacyMode) {
            return
        }

        state.shouldExpandInProgress = !state.shouldExpandInProgress
        view?.handleExpandInProgress(state.shouldExpandInProgress)

        view?.configInProgress(state.walletStatus?.receiving ?: 0, state.walletStatus?.sending
                ?: 0, !state.shouldExpandInProgress, state.privacyMode)
    }


    override fun initSubscriptions() {
        super.initSubscriptions()

        state.walletStatus = AppManager.instance.getStatus()

        view?.configTransactions(state.getTransactions(), state.privacyMode)
        view?.configWalletStatus(AppManager.instance.getStatus(),
                !state.shouldExpandAvailable,
                !state.shouldExpandInProgress, state.privacyMode)

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe(){
            state.walletStatus = AppManager.instance.getStatus()
            view?.configWalletStatus(AppManager.instance.getStatus(),
                    !state.shouldExpandAvailable,
                    !state.shouldExpandInProgress, state.privacyMode)
        }

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            view?.configTransactions(state.getTransactions(), state.privacyMode)
        }
    }

    override fun onDestroy() {
        view?.dismissAlert()
        super.onDestroy()
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, txStatusSubscription)

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
