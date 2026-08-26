package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Asset(val assetId: Int,
            var available: Long = 0L,
            var receiving: Long = 0L,
            var sending: Long = 0L,
            var maturing: Long = 0L,
            var shielded: Long = 0L,
            var maxPrivacy: Long = 0L,
            var updateLastTime: Long = 0L,
            var updateDone: Int = 0,
            var updateTotal: Int = 0,
            var system: SystemStateDTO = SystemStateDTO("", 0L)) : Parcelable {

    var unitName: String = ""
    var nthUnitName: String = ""
    var shortName: String = ""
    var shortDesc: String = ""
    var longDesc: String = ""
    var name: String = ""
    var site: String = ""
    var paper: String = ""
    var color:String = ""
    var image:Int = 0

    fun isBeam():Boolean {
        return assetId == 0
    }

    fun isBeamX():Boolean {
        return BuildConfig.FLAVOR == AppConfig.FLAVOR_MAINNET && assetId == 7
    }

    fun hasInProgressTransactions():Boolean {
        val last =  AssetManager.instance.getLastTransaction(assetId)
        return last?.isInProgress() == true
    }

    fun lockedSum():Long {
        return maturing + maxPrivacy + changedSum()
    }

    fun changedSum():Long {
        if (sending > 0 && receiving > 0) {
            return receiving
        }
        return 0L
    }


    fun usd():Long {
        return ExchangeManager.instance.exchangeValueUSDAsset(available, assetId)
    }

    fun dateUsed(): Long {
        return AssetManager.instance.getLastTransaction(assetId)?.createTime ?: 0L
    }

    fun blockChainUrl():String {
        return "https://explorer.beam.mw/assets/details/${assetId}"
    }
}