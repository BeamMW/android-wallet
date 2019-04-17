package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentInfoDTO(
        var senderId: String,
        var receiverId: String,
        var amount: Long,
        var kernelId: String,
        var isValid: Boolean,
        var rawProof: String) : Parcelable