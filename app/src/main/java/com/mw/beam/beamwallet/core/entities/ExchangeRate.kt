package com.mw.beam.beamwallet.core.entities

import android.content.Context
import android.os.Parcelable
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.dto.ExchangeRateDTO
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import kotlinx.android.parcel.Parcelize


enum class Currency(val value: Int) {
    Beam(1),
    Usd(2),
    Bitcoin(3),
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
            this == Beam -> {
                "BEAM"
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
            this == Beam -> {
                "BEAM"
            }
            else -> {
                "USD"
            }
        }
    }
}

@Parcelize
data class ExchangeRate(private val source: ExchangeRateDTO) : Parcelable {
    var currency = Currency.fromValue(source.to)
    var value = source.rate
    var realValue = source.rate.convertToBeam()
    var code = currency.shortName()
    var assetId = source.assetId
}