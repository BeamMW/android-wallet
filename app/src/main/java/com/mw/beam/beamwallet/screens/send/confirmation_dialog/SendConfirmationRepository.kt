package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class SendConfirmationRepository: BaseRepository(), SendConfirmationContract.Repository {

    override fun checkPassword(password: String): Boolean {
        return wallet?.checkWalletPassword(password) ?: false
    }

    override fun isFingerPrintEnabled(): Boolean {
        return getResult("isFingerPrintEnabled") {
            PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) && FingerprintManager.isManagerAvailable()
        }
    }

}