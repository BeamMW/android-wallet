package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AssetInfoDTO(
        var id: Int,
        var unitName: String,
        var nthUnitName: String,
        var shortName: String,
        var shortDesc: String,
        var longDesc: String,
        var name: String,
        var site: String,
        var paper: String) : Parcelable