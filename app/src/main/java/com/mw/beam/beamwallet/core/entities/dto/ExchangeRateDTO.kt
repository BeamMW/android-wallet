package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ExchangeRateDTO(
        var currency: Int,
        var unit: Int,
        var amount: Long,
        var updateTime: Long) : Parcelable
