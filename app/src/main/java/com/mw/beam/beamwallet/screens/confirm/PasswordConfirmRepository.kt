package com.mw.beam.beamwallet.screens.confirm

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.FaceIDManager
import com.mw.beam.beamwallet.core.helpers.FingerprintManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class PasswordConfirmRepository: BaseRepository(), PasswordConfirmContract.Repository {

    override fun isFingerPrintEnabled(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED) && FingerprintManager.isManagerAvailable()

    }

    override fun isFaceIDEnabled(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED)
                && FaceIDManager.isManagerAvailable()
    }

    override fun checkPassword(password: String): Boolean {
        return wallet?.checkWalletPassword(password) ?: false
    }
}