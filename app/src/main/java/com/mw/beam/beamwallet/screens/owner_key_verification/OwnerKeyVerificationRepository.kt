package com.mw.beam.beamwallet.screens.owner_key_verification

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

class OwnerKeyVerificationRepository: BaseRepository(), OwnerKeyVerificationContract.Repository {


    override fun checkPassword(pass: String): Boolean {
        return getResult("checkPassword") {
            wallet?.checkWalletPassword(pass) ?: false
        }
    }

    override fun isEnableFingerprint(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_IS_FINGERPRINT_ENABLED)
    }

}