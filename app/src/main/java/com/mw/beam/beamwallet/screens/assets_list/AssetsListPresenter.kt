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

package com.mw.beam.beamwallet.screens.assets_list

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Asset

import io.reactivex.disposables.Disposable

class AssetsListPresenter(view: AssetsListContract.View?, repository: AssetsListContract.Repository)
    : BasePresenter<AssetsListContract.View, AssetsListContract.Repository>(view, repository), AssetsListContract.Presenter {

    private lateinit var walletStatusSubscription: Disposable

    var filter = AssetFilter.recent_old

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }


    override fun onAssetPressed(asset: Asset) {
        view?.showAssetDetails(asset)
    }


    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.configAssets(getAssets())

        walletStatusSubscription = AppManager.instance.subOnStatusChanged.subscribe {
            view?.configAssets(getAssets())
        }
    }

    override fun onChangeFilter(filter: AssetFilter) {
        this.filter = filter
        view?.configAssets(getAssets())
    }

    fun getAssets():List<Asset> {
        when (filter) {
            AssetFilter.recent_old -> {
                return AssetManager.instance.assets.sortedBy {
                    it.dateUsed()
                }.reversed()
            }
            AssetFilter.old_recent -> {
                return AssetManager.instance.assets.sortedBy {
                    it.dateUsed()
                }
            }
            AssetFilter.amount_large_small -> {
                return AssetManager.instance.assets.sortedBy {
                    it.available
                }.reversed()
            }
            AssetFilter.amount_small_large -> {
                return AssetManager.instance.assets.sortedBy {
                    it.available
                }
            }
            AssetFilter.amount_usd_large -> {
                return AssetManager.instance.assets.sortedBy {
                    it.usd()
                }.reversed()
            }
            else -> {
                return AssetManager.instance.assets.sortedBy {
                    it.usd()
                }
            }
        }
    }

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

    override fun getSubscriptions(): Array<Disposable> = arrayOf(walletStatusSubscription)
}
