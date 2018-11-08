package com.mw.beam.beamwallet.welcomeScreen.welcomePhrases

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasesRepository : BaseRepository(), WelcomePhrasesContract.Repository {

    override fun getPhrases(): MutableList<String> {
        //TODO singleton
        return mutableListOf(
                "test 1",
                "test 2",
                "test 3",
                "test 4",
                "test 5",
                "test 6",
                "test 7",
                "test 8",
                "test 9",
                "test 10",
                "test 11",
                "test 12")
    }
}
