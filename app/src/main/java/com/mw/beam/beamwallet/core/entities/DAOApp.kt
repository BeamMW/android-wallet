package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DAOApp (
    val name:String?,
    val description: String?,
    var url: String?,
    val icon:String?,
    val api_version:String?,
    val min_api_version:String?,
    var support:Boolean?,
): Parcelable