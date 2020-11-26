package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeeChangeDTO(
        var fee: Long,
        var change: Long) : Parcelable