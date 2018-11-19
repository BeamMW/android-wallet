package com.mw.beam.beamwallet.core.entities

/**
 * Created by vain onnellinen on 019 19.11.18.
 */
data class WalletAddress(val walletID: ByteArray,
                         val label: String,
                         val category: String,
                         val createTime: Long,
                         val duration: Long,
                         val own: Boolean)