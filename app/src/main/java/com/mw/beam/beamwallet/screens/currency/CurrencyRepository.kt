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

package com.mw.beam.beamwallet.screens.currency

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class CurrencyRepository: BaseRepository(), CurrencyContract.Repository {

    override fun getCurrentCurrency(): Currency {
        return ExchangeManager.instance.currentCurrency()
    }

    override fun setCurrency(currency: Currency) {
        PreferencesManager.putLong(PreferencesManager.KEY_CURRENCY, currency.value.toLong())
        ExchangeManager.instance.currency = currency.value
    }
}