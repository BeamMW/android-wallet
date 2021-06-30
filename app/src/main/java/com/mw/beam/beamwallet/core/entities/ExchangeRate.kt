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
            this == Beam -> {
                "BEAM"
            }
            this == Usd -> {
                context.getString(R.string.usd)
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
            this == Beam -> {
                "BEAM"
            }
            this == Usd -> {
                "USD"
            }
            else -> {
                "USD"
            }
        }
    }
}

@Parcelize
data class ExchangeRate(private val source: ExchangeRateDTO) : Parcelable {
    var currency = if (source.toName == "btc") {
        Currency.Bitcoin
    }
    else {
        Currency.Usd
    }

    var value = source.rate
    var realValue = source.rate.convertToBeam()
    var code = currency.shortName()
    var assetId = if (source.fromName.contains("asset")) {
        val assetId = source.fromName.replace("asset_", "")
        assetId.toInt()
    }
    else {
        0
    }
    var fromName = source.fromName
    var toName = source.toName
}