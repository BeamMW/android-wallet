package com.mw.beam.beamwallet.welcomeScreen.welcomeOpen

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils


/**
 * Created by vain onnellinen on 10/19/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WelcomeOpenRepository : BaseRepository(), WelcomeOpenContract.Repository {

    override fun openWallet(pass: String?): AppConfig.Status {
        var result = AppConfig.Status.STATUS_ERROR

        if (!pass.isNullOrBlank()) {
            App.wallet = Api.openWallet(AppConfig.NODE_ADDRESS, AppConfig.DB_PATH, pass)

            if (wallet != null) {
                //TODO handle statuses
                wallet!!.syncWithNode()
                result = AppConfig.Status.STATUS_OK
            }
        }

        LogUtils.logResponse(result, object {}.javaClass.enclosingMethod.name)
        return result
    }
}
