package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.helpers.ChangeAction


data class OnAddressesDataWithAction(val action: ChangeAction, val addresses: List<WalletAddress>?)