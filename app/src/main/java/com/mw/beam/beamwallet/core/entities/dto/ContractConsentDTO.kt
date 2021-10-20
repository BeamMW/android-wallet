package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContractConsentDTO(
    var request: String,
    var info: String,
    var amounts: String) : Parcelable