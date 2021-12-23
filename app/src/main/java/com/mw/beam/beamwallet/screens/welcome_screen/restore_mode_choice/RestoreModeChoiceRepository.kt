package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.removeDatabase
import com.mw.beam.beamwallet.core.helpers.removeNodeDatabase

class RestoreModeChoiceRepository: BaseRepository(), RestoreModeChoiceContract.Repository {
    override fun removeWallet() {
        AppManager.instance.wallet = null

        PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS,"")
        PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)
        PreferencesManager.putBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        PreferencesManager.putString(PreferencesManager.KEY_TRANSACTIONS,"")

        removeDatabase()

        removeNodeDatabase()
    }

    override fun saveStartRestoreFlag() {
        PreferencesManager.putString(PreferencesManager.KEY_TRANSACTIONS,"")
        PreferencesManager.putBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE, true)
    }
}