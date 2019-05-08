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

package com.mw.beam.beamwallet.screens.main

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainPresenter(currentView: MainContract.View, currentRepository: MainContract.Repository)
    : BasePresenter<MainContract.View, MainContract.Repository>(currentView, currentRepository),
        MainContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.configNavDrawer()
    }

    override fun onClose() {
        repository.closeWallet()
    }

    override fun onChangePass() {
        view?.showChangePasswordScreen()
    }

    override fun onShowTransactionDetails(item: TxDescription) {
        view?.showTransactionDetails(item)
    }

    override fun onReceive() {
        view?.showReceiveScreen()
    }

    override fun onSend() {
        view?.showSendScreen()
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

    override fun hasStatus(): Boolean = true

}
