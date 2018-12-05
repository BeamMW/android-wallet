package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestoreRepository : BaseRepository(), WelcomeRestoreContract.Repository {
    //TODO handle it when implemented
    override var phrasesCount: Int = 12

    override fun recoverWallet(): Boolean {
        AppConfig.removeDatabase()
        return true
    }
}
