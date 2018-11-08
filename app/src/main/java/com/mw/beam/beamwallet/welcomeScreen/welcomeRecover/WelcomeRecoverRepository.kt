package com.mw.beam.beamwallet.welcomeScreen.welcomeRecover

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRecoverRepository : BaseRepository(), WelcomeRecoverContract.Repository {
    //TODO handle it when implemented
    override var phrasesCount: Int = 12

    override fun recoverWallet(): Boolean {
        //TODO implement
        return true
    }
}
