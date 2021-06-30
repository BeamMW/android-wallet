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

    var currency = Currency.Usd.value
    var isPrivacyMode = false

    init {
        val value = PreferencesManager.getLong(PreferencesManager.KEY_CURRENCY, 0)
        if(value == 0L) {
            PreferencesManager.putLong(PreferencesManager.KEY_CURRENCY, Currency.Usd.value.toLong())
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

    fun currentCurrency():Currency {
        return Currency.fromValue(currency)
    }

    fun currentRate():ExchangeRate? {
        return rates.firstOrNull {
            it.currency.value == currency
        }
    }

    fun getRate(id:Int?):ExchangeRate? {
        return rates.firstOrNull {
            it.currency.value == id
        }
    }

    fun exchangeValueUSDAsset(amount:Long, asset:Int):Long {
        if(amount == 0L || isPrivacyMode) {
            return  0L
        }

        rates.forEach {
            if(it.assetId == asset && it.currency == Currency.Usd) {
                return  it.value * amount;
            }
        }

        return 0L
    }
}