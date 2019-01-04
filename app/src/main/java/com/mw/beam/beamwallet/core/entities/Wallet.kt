package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

/**
 * Created by vain onnellinen on 10/2/18.
 */
data class Wallet(val _this: Long) {
    external fun getWalletStatus()
    external fun getUtxosStatus()
    external fun syncWithNode()
    external fun sendMoney(receiver: String, comment: String?, amount: Long, fee: Long)
    external fun calcChange(amount: Long)
    external fun getAddresses(own: Boolean)
    external fun generateNewAddress()
    external fun saveAddress(address: WalletAddressDTO, own : Boolean)
}
