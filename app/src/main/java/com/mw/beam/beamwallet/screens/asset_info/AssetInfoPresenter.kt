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

package com.mw.beam.beamwallet.screens.asset_info

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.Asset

import io.reactivex.disposables.Disposable

class AssetInfoPresenter(view: AssetInfoContract.View?, repository: AssetInfoContract.Repository)
    : BasePresenter<AssetInfoContract.View, AssetInfoContract.Repository>(view, repository), AssetInfoContract.Presenter {

    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable


    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.configAsset(getAsset())

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe {
            view?.configAsset(getAsset())
        }

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            view?.configAsset(getAsset())
        }
    }

    fun getAsset():Asset? {
        return AssetManager.instance.getAsset(view?.getAssetId() ?: -1)
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletStatusSubscription, txStatusSubscription)
}
