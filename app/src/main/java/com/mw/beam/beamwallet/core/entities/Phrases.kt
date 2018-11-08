package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 11/2/18.
 */
@Parcelize
data class Phrases(val phrases: MutableList<String>) : Parcelable
