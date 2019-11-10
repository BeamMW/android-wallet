package com.mw.beam.beamwallet.screens.confirm

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class PasswordConfirmRepository: BaseRepository(), PasswordConfirmContract.Repository {

    override fun isFingerPrintEnabled(): Boolean {
        return getResult("isFingerPrintEnabled") {
            PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) && FingerprintManager.isManagerAvailable()
        }
    }

    override fun checkPassword(password: String): Boolean {
        return wallet?.checkWalletPassword(password) ?: false
    }
}