package com.mw.beam.beamwallet.welcomeScreen.welcomeConfirm

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmRepository : BaseRepository(), WelcomeConfirmContract.Repository {
    override var phrases: Array<String>? = null

    override fun getPhrasesToValidate(): MutableList<Int> {
        return mutableListOf(1, 3, 4, 7, 10, 12)
    }
}
