package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category

data class SearchItem(val walletAddress: WalletAddress, var lastTransaction: TxDescription? = null, var category: Category? = null)