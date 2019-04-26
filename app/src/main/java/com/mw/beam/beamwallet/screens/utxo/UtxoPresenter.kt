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

package com.mw.beam.beamwallet.screens.utxo

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.Utxo
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoPresenter(currentView: UtxoContract.View, currentRepository: UtxoContract.Repository, private val state: UtxoState)
    : BasePresenter<UtxoContract.View, UtxoContract.Repository>(currentView, currentRepository),
        UtxoContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var blockchainInfoSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onStart() {
        super.onStart()
        notifyPrivacyStateChange()
    }

    private fun notifyPrivacyStateChange() {
        val privacyModeEnabled = isPrivacyModeEnabled()
        state.privacyMode = privacyModeEnabled
        view?.configPrivacyStatus(privacyModeEnabled)
    }

    override fun onChangePrivacyModePressed() {
        if (state.privacyMode) {
            setPrivacyModeEnabled(false)
            notifyPrivacyStateChange()
        } else {
            view?.showActivatePrivacyModeDialog()
        }
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        setPrivacyModeEnabled(true)
        notifyPrivacyStateChange()
    }

    override fun onUtxoPressed(utxo: Utxo) {
        view?.showUtxoDetails(utxo)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            view?.updateUtxos(utxos.reversed())
        }

        blockchainInfoSubscription = repository.getWalletStatus().subscribe { walletStatus ->
            view?.updateBlockchainInfo(walletStatus.system)
        }

    }

    override fun onDestroy() {
        view?.dismissAlert()
        super.onDestroy()
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, blockchainInfoSubscription)

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
