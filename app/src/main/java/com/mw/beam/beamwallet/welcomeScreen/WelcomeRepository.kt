package com.mw.beam.beamwallet.welcomeScreen

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/19/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WelcomeRepository : BaseRepository(), WelcomeContract.Repository {

    override fun isWalletInitialized(): Boolean {
        val result = Api.isWalletInitialized(AppConfig.DB_PATH)
        LogUtils.logResponse(result, object {}.javaClass.enclosingMethod.name)
        return result
    }
}
