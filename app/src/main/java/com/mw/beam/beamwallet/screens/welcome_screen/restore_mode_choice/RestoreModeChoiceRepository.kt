package com.mw.beam.beamwallet.screens.welcome_screen.restore_mode_choice

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.removeDatabase

class RestoreModeChoiceRepository: BaseRepository(), RestoreModeChoiceContract.Repository {
    override fun removeWallet() {
        removeDatabase()
    }

    override fun saveStartRestoreFlag() {
        PreferencesManager.putBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE, true)
    }
}