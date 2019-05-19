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

package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.utils.isBefore
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 19.11.18.
 */
@Parcelize
class WalletAddress(var source: WalletAddressDTO) : Parcelable {
    val walletID: String = source.walletID.replaceFirst(Regex("^0+"), "")
    var label: String = source.label
    val category: String = source.category
    val createTime: Long = source.createTime
    var duration: Long = source.duration
    val own: Long = source.own
    val isExpired = duration != 0L && ((createTime + duration) * 1000).isBefore()
    var isContact = own == 0L

    fun toDTO(): WalletAddressDTO = source.apply {
        this.label = this@WalletAddress.label
        this.duration = this@WalletAddress.duration
    }

    override fun toString(): String {
        return "\n\nWalletAddress(\n walletID=$walletID\n label=$label\n category=$category\n createTime=${CalendarUtils.fromTimestamp(createTime)}\n duration=$duration\n own=$own\n isExpired=$isExpired\n isContact=$isContact\n"
    }
}
