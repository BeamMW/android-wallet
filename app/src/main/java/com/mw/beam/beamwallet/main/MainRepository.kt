package com.mw.beam.beamwallet.main

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/4/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainRepository : BaseRepository(), MainContract.Repository {

    override fun closeWallet() {
        //TODO add method to close wallet
            LogUtils.log(object {}.javaClass.enclosingMethod.name)
    }
}
