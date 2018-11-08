package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.entities.Phrases

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeValidationRepository : BaseRepository(), WelcomeValidationContract.Repository {
    override var phrases: Phrases? = null

    override fun getPhrasesToValidate(): MutableList<Int> {
        return mutableListOf(1, 3, 4, 7, 10, 12)
    }
}
