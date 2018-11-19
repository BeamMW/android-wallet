package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeValidationRepository : BaseRepository(), WelcomeValidationContract.Repository {
    override var phrases: Array<String>? = null

    override fun getPhrasesToValidate(): MutableList<Int> {
        return mutableListOf(1, 3, 4, 7, 10, 12)
    }
}
