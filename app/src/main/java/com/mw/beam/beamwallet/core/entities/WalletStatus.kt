package com.mw.beam.beamwallet.core.entities

/**
 * Created by vain onnellinen on 10/9/18.
 */
data class WalletStatus(val available: Long,
                        val unconfirmed: Long,
                        val updateLastTime: Long,
                        val updateDone: Int,
                        val updateTotal: Int,
                        val system: SystemState)
