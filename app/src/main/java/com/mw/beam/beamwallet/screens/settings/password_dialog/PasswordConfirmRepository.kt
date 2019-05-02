package com.mw.beam.beamwallet.screens.settings.password_dialog

import com.mw.beam.beamwallet.base_screen.BaseRepository

class PasswordConfirmRepository: BaseRepository(), PasswordConfirmContract.Repository {
    override fun checkPassword(password: String): Boolean {
        return wallet?.checkWalletPassword(password) ?: false
    }
}