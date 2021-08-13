package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO


class Asset(val assetId: Int,
            var available: Long,
            var receiving: Long,
            var sending: Long,
            var maturing: Long,
            var shielded: Long,
            var maxPrivacy: Long,
            var updateLastTime: Long,
            var updateDone: Int,
            var updateTotal: Int,
            var system: SystemStateDTO) {

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

    fun hasInProgressTransactions():Boolean {
        val last =  AssetManager.instance.getLastTransaction(assetId)
        return last?.isInProgress() == true
    }

    fun lockedSum():Long {
        return maturing + maxPrivacy
    }

    fun usd():Long {
        return ExchangeManager.instance.exchangeValueUSDAsset(available, assetId)
    }

    fun dateUsed(): Long {
        return AssetManager.instance.getLastTransaction(assetId)?.createTime ?: 0L
    }
}