package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestoreRepository : BaseRepository(), WelcomeRestoreContract.Repository {

    override fun restoreWallet(): Boolean {
        AppConfig.removeDatabase()
        return true
    }
}
