package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.core.entities.WalletAddress
import java.io.Serializable

interface ChangeAddressCallback: Serializable {
    fun onChangeAddress(walletAddress: WalletAddress)
}