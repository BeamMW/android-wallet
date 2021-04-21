/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import com.mw.beam.beamwallet.core.entities.BMAddressType
import kotlinx.android.parcel.Parcelize

/**
 *  1/4/19.
 */
@Parcelize
data class TransactionParametersDTO(val address: String,
                                    val identity: String,
                                    val isPermanentAddress: Boolean,
                                    val isOffline: Boolean,
                                    var isShielded: Boolean,
                                    var isMaxPrivacy: Boolean,
                                    var isPublicOffline: Boolean,
                                    val amount: Long,
                                    var versionError: Boolean,
                                    var version: String,
                                    var addressType: Int) : Parcelable {

    fun getAddressType(): BMAddressType {
        return BMAddressType.findByValue(addressType) ?: BMAddressType.BMAddressTypeRegular
    }
}

