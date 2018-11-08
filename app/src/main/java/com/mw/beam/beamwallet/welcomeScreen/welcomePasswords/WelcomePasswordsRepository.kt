package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/23/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WelcomePasswordsRepository : BaseRepository(), WelcomePasswordsContract.Repository {

    override fun createWallet(pass: String?): AppConfig.Status {
        var result = AppConfig.Status.STATUS_ERROR

        if (!pass.isNullOrBlank()) {
            val wallet = Api.createWallet(AppConfig.NODE_ADDRESS, AppConfig.DB_PATH, pass!!, AppConfig.TEST_SEED)

            if (wallet != null) {
                setWallet(wallet)
                result = AppConfig.Status.STATUS_OK
            }
        }

        LogUtils.logResponse(result, object {}.javaClass.enclosingMethod.name)
        return result
    }
}
