package com.mw.beam.beamwallet.core.entities.dto

/**
 * Created by vain onnellinen on 1/4/19.
 */
data class WalletAddressDTO(val walletID: String,
                            var label: String,
                            val category: String,
                            val createTime: Long,
                            val duration: Long,
                            val own: Long) {
}
