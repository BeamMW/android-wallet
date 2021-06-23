package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExchangeRateDTO(
        var to: Int,
        var from: Int,
        var fromName: String,
        var toName: String,
        var assetId:Int,
        var rate: Long,
        var updateTime: Long) : Parcelable
