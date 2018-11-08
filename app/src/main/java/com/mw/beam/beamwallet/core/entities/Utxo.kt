package com.mw.beam.beamwallet.core.entities

/**
 * Created by vain onnellinen on 10/2/18.
 */
data class Utxo(val id: Long,
                val amount: Long,
                val status: Int,
                val createHeight: Long,
                val maturity: Long,
                val keyType: Int,
                val confirmHeight: Long,
                val confirmHash: ByteArray,
                val lockHeight: Long,
                val createTxId: ByteArray,
                val spentTxId: ByteArray)
