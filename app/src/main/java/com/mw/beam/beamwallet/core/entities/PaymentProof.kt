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
import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentProof(val txId: String, private val source: PaymentInfoDTO): Parcelable {
    val senderId: String = source.senderId
    val receiverId: String = source.receiverId
    val amount: Long = source.amount
    val kernelId: String = source.kernelId
    val isValid: Boolean = source.isValid
    val rawProof: String = source.rawProof

    override fun toString(): String {
        return "\n\nPaymentProof(\ntxId=$txId\n senderId=$senderId\n receiverId=$receiverId\n amount=$amount\n kernelId=$kernelId\n isValid=$isValid)"
    }
}