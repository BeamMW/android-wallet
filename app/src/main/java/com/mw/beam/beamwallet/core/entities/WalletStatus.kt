package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.WalletStatusDTO

/**
 * Created by vain onnellinen on 10/9/18.
 */
data class WalletStatus(private val source: WalletStatusDTO) {
    val available: Long = source.available
    val receiving: Long = source.receiving
    val sending: Long = source.sending
    val maturing: Long = source.maturing
    val updateLastTime: Long = source.updateLastTime
    val updateDone: Int = source.updateDone
    val updateTotal: Int = source.updateTotal
    val system: SystemState = SystemState(source.system)

    override fun toString(): String {
        return "available=$available receiving=$receiving sending=$sending maturing=$maturing updateLastTime=$updateLastTime updateDone=$updateDone updateTotal=$updateTotal system=$system"
    }
}
