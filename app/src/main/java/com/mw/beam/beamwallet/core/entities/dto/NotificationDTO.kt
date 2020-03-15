package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationDTO(
        var id: String,
        var state: Int,
        var createTime: Long) : Parcelable