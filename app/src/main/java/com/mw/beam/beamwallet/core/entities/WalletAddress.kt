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
import com.google.android.gms.common.stats.StatsEvent
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.utils.isBefore
import kotlinx.android.parcel.Parcelize

/**
 *  19.11.18.
 */

enum class BMAddressType(val value: Int) {
    BMAddressTypeRegular(0),
    BMAddressTypeMaxPrivacy(1),
    BMAddressTypeShielded(2),
    BMAddressTypeOfflinePublic(3),
    BMAddressTypeRegularPermanent(4),
    BMAddressTypeUnknown(5);

    companion object {
        private val types = values().associateBy { it.value }
        fun findByValue(value: Int) = types[value]
    }
}


@Parcelize
class WalletAddress(var source: WalletAddressDTO) : Parcelable {
    private val walletID: String = source.walletID.replaceFirst(Regex("^0+"), "")

    var label: String = source.label
    var createTime: Long = source.createTime
    var duration: Long = source.duration
    val own: Long = source.own
    var isExpired = duration != 0L && ((createTime + duration) * 1000).isBefore()
    var isContact = own == 0L
    var tokenOffline = ""
    var tokenMaxPrivacy = ""
    var identity = source.identity
    var address = source.address
    var displayAddress:String? = null

    fun toDTO(): WalletAddressDTO = source.apply {
        this.label = this@WalletAddress.label
        this.duration = this@WalletAddress.duration
        this.identity = this@WalletAddress.identity
        this.address = this@WalletAddress.address
    }

    private fun getWalletID(): String {
        if(walletID.isEmpty()) {
            return address
        }
        return walletID
    }

    val getOriginalId: String
        get() = this.walletID

    val id: String
        get() = this.getWalletID()

    override fun toString(): String {
        return "\n\nWalletAddress(\n walletID=$walletID\n label=$label\n createTime=${CalendarUtils.fromTimestamp(createTime)}\n duration=$duration\n own=$own\n isExpired=$isExpired\n isContact=$isContact\n"
    }
}
