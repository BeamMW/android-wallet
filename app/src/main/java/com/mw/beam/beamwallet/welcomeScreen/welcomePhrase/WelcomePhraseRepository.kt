package com.mw.beam.beamwallet.welcomeScreen.welcomePhrase

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhraseRepository : BaseRepository(), WelcomePhraseContract.Repository {
    override val phrases: Array<String> = Api.createMnemonic()
}
