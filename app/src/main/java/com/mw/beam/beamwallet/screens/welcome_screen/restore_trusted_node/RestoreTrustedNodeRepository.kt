package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import io.reactivex.subjects.Subject
import com.mw.beam.beamwallet.core.listeners.WalletListener

class RestoreTrustedNodeRepository: BaseRepository(), RestoreTrustedNodeContract.Repository {
    override fun connectToNode(address: String) {
        getResult("changeNodeAddress") {
            AppConfig.NODE_ADDRESS = address
            AppManager.instance.wallet?.changeNodeAddress(AppConfig.NODE_ADDRESS)
            PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)
            PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
            PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, address)
        }
    }

    override fun getNodeConnectionStatusChanged(): Subject<Boolean> {
        return getResult(WalletListener.subOnNodeConnectedStatusChanged, "getNodeConnectionStatusChanged")
    }
}