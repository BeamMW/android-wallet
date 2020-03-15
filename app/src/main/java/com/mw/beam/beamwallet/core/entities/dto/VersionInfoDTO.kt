package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VersionInfoDTO(
        var application: Int,
        var versionMajor: Long,
        var versionMinor: Long,
        var versionRevision: Long) : Parcelable