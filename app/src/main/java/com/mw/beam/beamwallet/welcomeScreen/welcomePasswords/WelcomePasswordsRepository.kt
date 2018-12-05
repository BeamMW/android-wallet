package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import com.mw.beam.beamwallet.baseScreen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LogUtils

/**
 * Created by vain onnellinen on 10/23/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WelcomePasswordsRepository : BaseRepository(), WelcomePasswordsContract.Repository {
    override var phrases: Array<String>? = null

    override fun createWallet(pass: String?, phrases: String?): AppConfig.Status {
        var result = AppConfig.Status.STATUS_ERROR

        if (!pass.isNullOrBlank() && phrases != null) {
            if (Api.isWalletInitialized(AppConfig.DB_PATH)) {
                AppConfig.removeDatabase()
            }

            App.wallet = Api.createWallet(AppConfig.NODE_ADDRESS, AppConfig.DB_PATH, pass, phrases)

            if (wallet != null) {
                //TODO handle statuses of process
                wallet!!.syncWithNode()
                result = AppConfig.Status.STATUS_OK
            }
        }

        LogUtils.logResponse(result, object {}.javaClass.enclosingMethod.name)
        return result
    }
}
