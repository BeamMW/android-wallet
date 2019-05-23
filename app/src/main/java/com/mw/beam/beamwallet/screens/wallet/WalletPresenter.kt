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
import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.utils.TransactionFields
import io.reactivex.disposables.Disposable


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletPresenter(currentView: WalletContract.View, currentRepository: WalletContract.Repository, private val state: WalletState)
    : BasePresenter<WalletContract.View, WalletContract.Repository>(currentView, currentRepository),
        WalletContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
        state.privacyMode = repository.isPrivacyModeEnabled()
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
        state.shouldExpandAvailable = state.privacyMode
        state.shouldExpandInProgress = state.privacyMode

        view?.handleExpandAvailable(state.privacyMode)
        view?.handleExpandInProgress(state.privacyMode)

        if (!privacyModeEnabled) {
           state.walletStatus?.let { view?.configWalletStatus(it, privacyModeEnabled) }
        }
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

    override fun onWhereBuyBeamPressed() {
        if (repository.isAllowOpenExternalLink()) {
            view?.closeDrawer()
            view?.openExternalLink(AppConfig.BEAM_EXCHANGES_LINK)
        } else {
            view?.showOpenLinkAlert()
        }
    }

    override fun onOpenLinkPressed() {
        view?.closeDrawer()
        view?.openExternalLink(AppConfig.BEAM_EXCHANGES_LINK)
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
        view?.showTransactionDetails(txDescription)
    }

    override fun onExpandAvailablePressed() {
        if (state.privacyMode) {
            return
        }

        state.shouldExpandAvailable = !state.shouldExpandAvailable
        view?.handleExpandAvailable(state.shouldExpandAvailable)

        if (!state.shouldExpandAvailable) {
            view?.configAvailable(state.walletStatus?.available ?: 0, state.walletStatus?.maturing ?: 0, state.privacyMode)
        }
    }

    override fun onExpandInProgressPressed() {
        if (state.privacyMode) {
            return
        }

        state.shouldExpandInProgress = !state.shouldExpandInProgress
        view?.handleExpandInProgress(state.shouldExpandInProgress)

        if (!state.shouldExpandInProgress) {
            view?.configInProgress(state.walletStatus?.receiving ?: 0, state.walletStatus?.sending
                    ?: 0, state.privacyMode)
        }
    }

    override fun onTransactionsMenuButtonPressed(menu: View) {
        view?.showTransactionsMenu(menu, state.transactions.isNullOrEmpty())
    }

    override fun onTransactionsMenuPressed(item: MenuItem): Boolean {
        return view?.handleTransactionsMenu(item) ?: false
    }

    override fun onSearchPressed() = toDo()
    override fun onFilterPressed() = toDo()
    override fun onDeletePressed() = toDo()

    override fun onExportPressed() {
        val file = repository.getTransactionsFile()
        val stringBuilder = StringBuilder()
        stringBuilder.append(TransactionFields.HEAD_LINE)

        state.transactions.values.sortedByDescending { it.modifyTime }.forEach {
            stringBuilder.append(TransactionFields.formatTransaction(it))
        }
        file.writeBytes(stringBuilder.toString().toByteArray())

        view?.showShareFileChooser(file)
    }

    override fun onProofVerificationPressed() {
        view?.showProofVerification()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletStatusSubscription = repository.getWalletStatus().subscribe {
            state.walletStatus = it
            view?.configWalletStatus(it, state.privacyMode)
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            view?.configTransactions(
                    when (data.action) {
                        ChangeAction.REMOVED -> state.deleteTransaction(data.tx)
                        else -> state.updateTransactions(data.tx)
                    }, state.privacyMode)
        }
    }

    override fun onDestroy() {
        view?.dismissAlert()
        super.onDestroy()
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletStatusSubscription, txStatusSubscription)

    private fun toDo() {
        view?.showSnackBar("Coming soon...")
    }

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
