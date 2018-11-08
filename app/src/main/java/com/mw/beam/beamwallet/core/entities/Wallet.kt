package com.mw.beam.beamwallet.core.entities

/**
 * Created by vain onnellinen on 10/2/18.
 */
data class Wallet(val _this: Long) {
    external fun sendMoney()
    external fun sendMoney2()
    external fun syncWithNode()
    external fun calcChange()
    external fun getWalletStatus()
    external fun getUtxosStatus()
    external fun getAddresses()
    external fun cancelTx()
    external fun deleteTx()
    external fun createNewAddress()
    external fun generateNewWalletID()
    external fun changeCurrentWalletIDs()
    external fun deleteAddress()
    external fun deleteOwnAddress()
    external fun setNodeAddress()
    external fun emergencyReset()
    external fun changeWalletPassword()
}
