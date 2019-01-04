package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.entities.dto.UtxoDTO
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToString
import com.mw.beam.beamwallet.core.helpers.toHex
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 10/2/18.
 */
@Parcelize
data class Utxo(private val source: UtxoDTO) : Parcelable {
    val id: Long = source.id
    val amount: Long = source.amount
    val status: UtxoStatus = UtxoStatus.fromValue(source.status)
    val createHeight: Long = source.createHeight
    val maturity: Long = source.maturity
    val keyType: UtxoKeyType = UtxoKeyType.fromValue(source.keyType.convertToString())
    val confirmHeight: Long = source.confirmHeight
    val confirmHash: ByteArray? = source.confirmHash
    val lockHeight: Long = source.lockHeight
    val createTxId: String = source.createTxId.toHex()
    val spentTxId: String? = source.spentTxId?.toHex()
}
