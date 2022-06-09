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
        val asset =  AssetManager.instance.getAsset(view?.getAssetId() ?: -1)
        if (asset?.isBeamX() == true) {
            asset.shortName = "BEAMX"
            asset.shortDesc = "BeamX DAO governance token"
            asset.longDesc = "BEAMX token is a Confidential Asset issued on top of the Beam blockchain with a fixed emission of 100,000,000 units (except for the lender of a \"last resort\" scenario). BEAMX is the governance token for the BeamX DAO, managed by the BeamX DAO Core contract. Holders can earn BeamX tokens by participating in the DAO activities: providing liquidity to the DeFi applications governed by the DAO or participating in the governance process."
            asset.site = "https://www.beamxdao.org/"
            asset.paper = "https://documentation.beam.mw/overview/beamx-tokenomics"
        }
        return asset
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
            view?.configTransactions(getTransactions())
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
