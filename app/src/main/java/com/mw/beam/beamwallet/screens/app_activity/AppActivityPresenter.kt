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

package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App

class AppActivityPresenter(view: AppActivityContract.View?, repository: AppActivityContract.Repository) : BasePresenter<AppActivityContract.View, AppActivityContract.Repository>(view, repository), AppActivityContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        if (repository.isWalletInitialized() && !App.isAuthenticated) {
            view?.showOpenFragment()
        } else if (App.isAuthenticated) {
            view?.showWalletFragment()
        }
    }

    override fun onNewIntent(txId: String?) {
        if (App.isAuthenticated) {
            if (txId == null) {
                view?.showWalletFragment()
            } else {
                view?.showTransactionDetailsFragment(txId)
            }
        } else {
            view?.showOpenFragment()
        }
    }

    override fun onPendingSend(info: PendingSendInfo) {
        view?.startNewSnackbar({}, { repository.sendMoney(info.outgoingAddress, info.token, info.comment, info.amount, info.fee) })
    }
}