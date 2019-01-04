package com.mw.beam.beamwallet.core.entities.dto

/**
 * Created by vain onnellinen on 1/4/19.
 */
class WalletStatusDTO(val available: Long,
                      val unconfirmed: Long,
                      val updateLastTime: Long,
                      val updateDone: Int,
                      val updateTotal: Int,
                      val system: SystemStateDTO) {
}
