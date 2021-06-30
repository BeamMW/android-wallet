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

package com.mw.beam.beamwallet.screens.asset_detail

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Asset

import io.reactivex.disposables.Disposable

class AssetDetailPresenter(view: AssetDetailContract.View?, repository: AssetDetailContract.Repository)
    : BasePresenter<AssetDetailContract.View, AssetDetailContract.Repository>(view, repository), AssetDetailContract.Presenter {

    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable


    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.configAsset(getAsset())
        view?.configTransactions(getTransactions())

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe {
            view?.configAsset(getAsset())
            view?.configTransactions(getTransactions())
        }

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            view?.configAsset(getAsset())
            view?.configTransactions(getTransactions())
        }
    }

    fun getAsset():Asset? {
        return AssetManager.instance.getAsset(view?.getAssetId() ?: -1)
    }

    fun getTransactions() = AppManager.instance.getTransactions().filter {
        it.assetId == view?.getAssetId()
    }.sortedByDescending { it.createTime }.take(4)

    override fun onChangePrivacyModePressed() {
        if (!ExchangeManager.instance.isPrivacyMode && repository.isNeedConfirmEnablePrivacyMode()) {
            view?.showActivatePrivacyModeDialog()
        } else {
            ExchangeManager.instance.isPrivacyMode = !ExchangeManager.instance.isPrivacyMode
            repository.setPrivacyModeEnabled(ExchangeManager.instance.isPrivacyMode)
            view?.configPrivacyStatus()
        }
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        ExchangeManager.instance.isPrivacyMode = true
        repository.setPrivacyModeEnabled(true)
        view?.configPrivacyStatus()
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletStatusSubscription, txStatusSubscription)
}
