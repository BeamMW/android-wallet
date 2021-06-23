package com.mw.beam.beamwallet.core

import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class ExchangeManager {

    companion object {
        private var INSTANCE: ExchangeManager? = null

        val instance: ExchangeManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = ExchangeManager()
                }

                return INSTANCE!!
            }
    }

    var currency = Currency.Usd.ordinal
    var isPrivacyMode = false

    init {
        val value = PreferencesManager.getLong(PreferencesManager.KEY_CURRENCY, 0)
        if(value == 0L) {
            PreferencesManager.putLong(PreferencesManager.KEY_CURRENCY, Currency.Usd.ordinal.toLong())
        }
        else {
            currency = value.toInt()
        }

        isPrivacyMode = PreferencesManager.getBoolean(PreferencesManager.KEY_PRIVACY_MODE, false)
    }

    var rates = mutableListOf<ExchangeRate>()

    fun isCurrenciesAvailable(): Boolean {
        return rates.size > 0
    }

    fun currentRate():ExchangeRate? {
        return rates.firstOrNull {
            it.currency.ordinal == currency
        }
    }
}