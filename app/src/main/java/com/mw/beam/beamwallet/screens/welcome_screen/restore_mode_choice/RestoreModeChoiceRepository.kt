package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.removeDatabase
import com.mw.beam.beamwallet.core.helpers.removeNodeDatabase

class RestoreModeChoiceRepository: BaseRepository(), RestoreModeChoiceContract.Repository {
    override fun removeWallet() {
        App.wallet = null

        PreferencesManager.putString("",PreferencesManager.KEY_NODE_ADDRESS)

        removeDatabase()

        removeNodeDatabase()
    }

    override fun saveStartRestoreFlag() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE, true)
    }
}