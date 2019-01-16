package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

/**
 * Created by vain onnellinen on 019 19.11.18.
 */
class WalletAddress(var source: WalletAddressDTO) {
    val walletID: String = source.walletID.replaceFirst(Regex("^0+"), "")
    var label: String = source.label
    val category: String = source.category
    val createTime: Long = source.createTime
    var duration: Long = source.duration
    val own: Long = source.own

    fun toDTO(): WalletAddressDTO = source.apply {
        this.label = this@WalletAddress.label
    }
}
