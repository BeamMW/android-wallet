package com.mw.beam.beamwallet.welcomeScreen.welcomePhrases

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasesRepository : BaseRepository(), WelcomePhrasesContract.Repository {
    override val phrases: Array<String> = Api.createMnemonic()
}
