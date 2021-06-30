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

package com.mw.beam.beamwallet.core.helpers

import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Currency

import kotlin.math.roundToInt

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.roundToLong

fun Long.convertToBeamString(): String = (this.toDouble() / 100000000).convertToBeamString()
fun Double.convertToBeamString(): String = DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(this)
fun Long.convertToBeam(): Double = this.toDouble() / 100000000
fun Long.convertToBeamWithSign(isSent: Boolean) = if (isSent) "-${this.convertToBeamString()}" else "+${this.convertToBeamString()}"
fun Double.convertToGroth() = (this * 100000000).roundToLong()

fun Long.convertToAssetString(name:String): String {
    if (ExchangeManager.instance.isPrivacyMode) {
        return name
    }
    return (this.toDouble() / 100000000).convertToBeamString() + " " + name
}

fun Long.exchangeValueAsset(assetId:Int): String {
    if (this == 0L || ExchangeManager.instance.isPrivacyMode) {
        return ""
    }

   ExchangeManager.instance.rates.forEach {
       if (it.currency.value == ExchangeManager.instance.currency && it.assetId == assetId) {
           val value = it.value.toDouble() / 100000000
           val beam = this.convertToBeam()
           val rate = value * beam
           if (it.currency == Currency.Usd) {
               return  DecimalFormat("#.##").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " USD"
           }
           else if (it.currency == Currency.Bitcoin) {
               return  DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " BTC"
           }
       }
   }

    return ""
}

fun Long.convertToCurrencyString(): String? {
    val current = ExchangeManager.instance.currentRate() ?: return null

    if (this != 0L) {
        val value = current.value.toDouble() / 100000000
        val beam = this.convertToBeam()
        val rate = value * beam
        if (current.currency == Currency.Usd) {
            return  DecimalFormat("#.##").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " USD"
        }
        else if (current.currency == Currency.Bitcoin) {
            return  DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " BTC"
        }
    }
    else if (this == 0L) {
        if (ExchangeManager.instance.currency == Currency.Usd.value) {
            return  "-USD"
        }
        else if (ExchangeManager.instance.currency == Currency.Bitcoin.value) {
            return  "-BTC"
        }
    }

    return null
}

fun Long.convertToCurrencyGrothString(): String? {
    val current = ExchangeManager.instance.currentRate() ?: return null

    if (this != 0L) {
        val value = current.value.toDouble() / 100000000
        val beam = this.convertToBeam()
        var rate = value * beam
        if (current.currency == Currency.Usd) {
            if(rate < 0.01) {
                return "< 1 cent";
            }
            return  DecimalFormat("#.##").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " USD"
        }
        else if (current.currency == Currency.Bitcoin) {
            val resultString = DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " BTC"
            if(resultString == "0 BTC") {
                rate *= 100000000;
                return DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " satoshis"
            }

            return resultString
        }
    }
    else if (this == 0L) {
        if (ExchangeManager.instance.currency == Currency.Usd.value) {
            return  "-USD"
        }
        else if (ExchangeManager.instance.currency == Currency.Bitcoin.value) {
            return  "-BTC"
        }
    }

    return null
}

fun Double.convertToCurrencyString(currency: Currency?): String? {
    if(currency == Currency.Beam) {
        return convertToCurrencyString()
    }
    val entered = this
    val current = ExchangeManager.instance.getRate(currency?.value)
    val value = current?.value?.toDouble()?.div(100000000) ?: return null
    val rate = (entered)/value
    return DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " BEAM"
}

fun Double.convertToCurrencyString(): String? {
    val current = ExchangeManager.instance.currentRate() ?: return null

    if (this != 0.0) {
        val value = current.value.toDouble() / 100000000
        val rate = value * this
        if (current.currency == Currency.Usd) {
            return  DecimalFormat("#.##").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " USD"
        }
        else if (current.currency == Currency.Bitcoin) {
            return  DecimalFormat("#.########").apply { decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US) }.format(rate) + " BTC"
        }
    }
    else if (this == 0.0) {
        if (ExchangeManager.instance.currency == Currency.Usd.value) {
            return  "-USD"
        }
        else if (ExchangeManager.instance.currency == Currency.Bitcoin.value) {
            return  "-BTC"
        }
    }

    return null
}
