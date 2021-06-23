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

import android.app.Application
import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.OnboardManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.runOnUiThread


/**
 *  10/1/18.
 */
class WalletPresenter(currentView: WalletContract.View, currentRepository: WalletContract.Repository, private val state: WalletState)
    : BasePresenter<WalletContract.View, WalletContract.Repository>(currentView, currentRepository),
        WalletContract.Presenter {

    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable
    private lateinit var faucetGeneratedSubscription: Disposable
    private lateinit var subOnCurrenciesSubscription: Disposable


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

        view?.selectWalletMenu()

        val canReceive = OnboardManager.instance.canReceiveFaucet() && state.getTransactions().count() == 0
        view?.showFaucet(canReceive)
        view?.showSecure(OnboardManager.instance.canMakeSecure())

        view?.configWalletStatus()

    }

    override fun onStart() {
        super.onStart()

        notifyPrivacyStateChange()
    }

    private fun notifyPrivacyStateChange() {
        val privacyModeEnabled = repository.isPrivacyModeEnabled()
        if(privacyModeEnabled == state.privacyMode) {
            return
        }
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

    override fun onReceiveFaucet() {
        view?.showReceiveFaucet()
    }

    override fun generateFaucetAddress() {
        AppManager.instance.createAddressForFaucet()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.walletStatus = AppManager.instance.getStatus()

        view?.configTransactions(state.getTransactions())
        view?.configWalletStatus()

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe {
            state.walletStatus = AppManager.instance.getStatus()
            view?.configWalletStatus()

            val canReceive = OnboardManager.instance.canReceiveFaucet() && state.getTransactions().count() == 0
            view?.showFaucet(canReceive)
            view?.showSecure(OnboardManager.instance.canMakeSecure())
        }

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            view?.configTransactions(state.getTransactions())
        }

        faucetGeneratedSubscription = AppManager.instance.subOnFaucedGenerated.subscribe {
          AppActivity.self.runOnUiThread {
              val link =  when (BuildConfig.FLAVOR) {
                  AppConfig.FLAVOR_MAINNET -> "https://faucet.beamprivacy.community/?address=$it&type=mainnet&redirectUri=app://open.mainnet.app"
                  AppConfig.FLAVOR_TESTNET -> "https://faucet.beamprivacy.community/?address=$it&type=testnet&redirectUri=app://open.testnet.app"
                  else -> "https://faucet.beamprivacy.community/?address=$it&type=masternet&redirectUri=app://open.master.app"
              }
              view?.onFaucetAddressGenerated(link)
          }
        }

        subOnCurrenciesSubscription = AppManager.instance.subOnCurrenciesChanged.subscribe {
            App.self.runOnUiThread {
                view?.configTransactions(state.getTransactions())
                view?.configWalletStatus()
            }
        }
    }

    override fun onSecure() {
        view?.showSeedScreen()
    }

    override fun onDestroy() {
        view?.dismissAlert()
        super.onDestroy()
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletStatusSubscription, txStatusSubscription, faucetGeneratedSubscription, subOnCurrenciesSubscription)

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
