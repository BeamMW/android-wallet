package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 1/4/19.
 */
@Parcelize
data class UtxoDTO(val id: Long,
                   val amount: Long,
                   val status: Int,
                   val createHeight: Long,
                   val maturity: Long,
                   val keyType: Int,
                   val confirmHeight: Long,
                   val confirmHash: ByteArray?,
                   val lockHeight: Long,
                   val createTxId: ByteArray?,
                   val spentTxId: ByteArray?) : Parcelable
