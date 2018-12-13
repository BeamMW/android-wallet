package com.mw.beam.beamwallet.welcomeScreen.welcomeConfirm

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmRepository : BaseRepository(), WelcomeConfirmContract.Repository {
    override var phrases: Array<String>? = null

    override fun getPhrasesToValidate(): List<Int> {
        val set = HashSet<Int>()
        val ran = Random()

        while (set.size < 6) {
            set.add(ran.nextInt(11) + 1)
        }

        return set.shuffled()
    }
}
