package com.mw.beam.beamwallet.core.entities

import android.content.Context
import android.os.Parcelable
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.dto.ExchangeRateDTO
import kotlinx.android.parcel.Parcelize


enum class Currency(val value: Int) {
    Beam(0),
    Bitcoin(1),
    Litecoin(3),
    Usd(4),
    Off(-1);

    companion object {
        private val map: HashMap<Int, Currency> = HashMap()

        init {
            values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): Currency {
            return map[type] ?: Usd
        }
    }

    fun name(context:Context): String {
        return when {
            this == Bitcoin -> {
                context.getString(R.string.btc)
            }
            this == Off -> {
                context.getString(R.string.off)
            }
            else -> {
                context.getString(R.string.usd)
            }
        }
    }

    fun shortName(): String {
        return when {
            this == Bitcoin -> {
                "BTC"
            }
            this == Off -> {
                ""
            }
            else -> {
                "USD"
            }
        }
    }
}

@Parcelize
data class ExchangeRate(private val source: ExchangeRateDTO) : Parcelable {
    var currency:Currency = Currency.fromValue(source.unit)
    var unit = source.unit
    var amount = source.amount
}