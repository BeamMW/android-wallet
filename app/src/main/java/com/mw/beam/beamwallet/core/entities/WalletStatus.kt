package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.WalletStatusDTO

/**
 * Created by vain onnellinen on 10/9/18.
 */
data class WalletStatus(private val source: WalletStatusDTO){
    val available: Long = source.available
    val unconfirmed: Long = source.unconfirmed
    val updateLastTime: Long = source.updateLastTime
    val updateDone: Int = source.updateDone
    val updateTotal: Int = source.updateTotal
    val system: SystemState = SystemState(source.system)

    override fun toString(): String {
        return "available=$available unconfirmed=$unconfirmed updateLastTime=$updateLastTime updateDone=$updateDone updateTotal=$updateTotal system=$system"
    }
}
