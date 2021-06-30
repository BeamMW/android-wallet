package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExchangeRateDTO(
        var fromName: String,
        var toName: String,
        var rate: Long,
        var updateTime: Long) : Parcelable
