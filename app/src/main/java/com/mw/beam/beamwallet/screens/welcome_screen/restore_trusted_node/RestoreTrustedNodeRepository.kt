package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.App

class RestoreTrustedNodeRepository: BaseRepository(), RestoreTrustedNodeContract.Repository {
    override fun connectToNode(address: String) {
        getResult("changeNodeAddress") {
            App.wallet?.changeNodeAddress(address)
        }
    }
}