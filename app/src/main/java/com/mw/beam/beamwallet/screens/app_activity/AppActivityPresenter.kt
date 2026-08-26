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
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.AppManager

class AppActivityPresenter(view: AppActivityContract.View?, repository: AppActivityContract.Repository) : BasePresenter<AppActivityContract.View, AppActivityContract.Repository>(view, repository), AppActivityContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()

        // An unfinished restore is cleaned up on a cold start by App.onCreate, which drops the
        // database and clears the flag before any activity exists — and by WelcomeOpenPresenter
        // when the user lands back on the open screen. Seeing the flag here therefore means a
        // restore is in flight in this very process and the activity was merely recreated (theme
        // or locale change, system reclaim). Deleting the database then destroys the wallet being
        // restored, so leave it, and leave the restored back stack to carry on where it was.
        if (!PreferencesManager.getBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE)) {
            if (repository.isWalletInitialized() && !App.isAuthenticated) {
                view?.showOpenFragment()
            }
            else if (App.isAuthenticated) {
                view?.showWalletFragment()
            }
        }
    }

    override fun onNewIntent(txId: String?) {
        if (!PreferencesManager.getBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE)
                && repository.isWalletInitialized()) {
            if (App.isAuthenticated) {
                if (txId != null) {
                    view?.showTransactionDetailsFragment(txId)
                }
            }
        }
    }

    override fun onPendingSend(info: PendingSendInfo) {
        AppManager.instance.lastSendingAddress = info.token

        view?.startNewSnackbar(info.assetId, { repository.cancelSendMoney(info.token) }, { repository.sendMoney(info.outgoingAddress, info.token, info.comment, info.amount, info.fee, info.saveAddress, info.assetId, info.isOffline) })
    }
}