package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToString
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 10/2/18.
 */
@Parcelize
data class Utxo(val id: Long,
                val amount: Long,
                val status: Int,
                val createHeight: Long,
                val maturity: Long,
                val keyType: Int,
                val confirmHeight: Long,
                val confirmHash: ByteArray?,
                val lockHeight: Long,
                val createTxId: ByteArray,
                val spentTxId: ByteArray?) : Parcelable {
    val statusEnum: UtxoStatus
        get() = UtxoStatus.fromValue(status)
    val keyTypeEnum: UtxoKeyType
        get() = UtxoKeyType.fromValue(keyType.convertToString())
}
