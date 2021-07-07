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

package com.mw.beam.beamwallet.screens.choose_currency

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import com.mw.beam.beamwallet.core.entities.dto.ExchangeRateDTO
import com.mw.beam.beamwallet.core.helpers.LocaleHelper

class ChooseCurrencyPresenter(view: ChooseCurrencyContract.View?, repository: ChooseCurrencyContract.Repository)
    : BasePresenter<ChooseCurrencyContract.View, ChooseCurrencyContract.Repository>(view, repository),
        ChooseCurrencyContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init(AssetManager.instance.assets)
    }

    override fun onSelectAsset(assetId: Int) {
        view?.changeAsset(assetId)
    }
}