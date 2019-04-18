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