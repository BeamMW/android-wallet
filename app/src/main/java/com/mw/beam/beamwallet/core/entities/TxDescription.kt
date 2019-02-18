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
import com.mw.beam.beamwallet.core.entities.dto.TxDescriptionDTO
import com.mw.beam.beamwallet.core.helpers.TxFailureReason
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 10/2/18.
 */
@Parcelize
class TxDescription(private val source: TxDescriptionDTO) : Parcelable {
    val id: String = source.id
    val amount: Long = source.amount
    val fee: Long = source.fee
    val change: Long = source.change
    val minHeight: Long = source.minHeight
    val peerId: String = source.peerId.replaceFirst(Regex("^0+"), "")
    val myId: String = source.myId.replaceFirst(Regex("^0+"), "")
    val message: String = source.message ?: ""
    val createTime: Long = source.createTime
    val modifyTime: Long = source.modifyTime
    val sender: TxSender = TxSender.fromValue(source.sender)
    val status: TxStatus = TxStatus.fromValue(source.status)
    val kernelId: String = source.kernelId
    val selfTx: Boolean = source.selfTx
    val failureReason: TxFailureReason = TxFailureReason.fromValue(source.failureReason)

    override fun toString(): String {
        return "TxDescription(id=$id amount=$amount fee=$fee status=${status.name} kernelId=$kernelId change=$change minHeight=$minHeight " +
                "peerId=$peerId myId=$myId message=$message createTime=$createTime modifyTime=$modifyTime sender=${sender.name} selfTx=$selfTx failureReason=$failureReason)"
    }
}
