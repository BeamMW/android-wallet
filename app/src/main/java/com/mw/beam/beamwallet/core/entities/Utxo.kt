/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.entities.dto.UtxoDTO
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToString
import kotlinx.android.parcel.Parcelize

/**
 *  10/2/18.
 */
@Parcelize
data class Utxo(private val source: UtxoDTO) : Parcelable {
    val id: Long = source.id
    val stringId: String = source.stringId.replaceFirst(Regex("^0+"), "")
    val amount: Long = source.amount
    val status: UtxoStatus = UtxoStatus.fromValue(source.status)
    val maturity: Long = source.maturity
    var keyType: UtxoKeyType = UtxoKeyType.fromValue(source.keyType.convertToString())
    val confirmHeight: Long = source.confirmHeight
    val createTxId: String? = source.createTxId
    val spentTxId: String? = source.spentTxId
    var txoID: Long = source.txoID
    var timeLeft: String? = null
    var time: Long = 0L
    var assetId = source.assetId
    var isShielded = source.isShielded

    var transactionDate:Long? = null
    var transactionComment:String? = null

    override fun toString(): String {
        return "\n\nUTXO(\n id=$id\n stringId=$stringId\n amount=$amount\n status=${status.name}\n maturity=$maturity\n keyType=${keyType.name}\n confirmHeight=$confirmHeight\n " +
                "createTxId=$createTxId\n spentTxId=$spentTxId)"
    }
}
