package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.BaseRepository

class SendConfirmationRepository: BaseRepository(), SendConfirmationContract.Repository {

    override fun checkPassword(password: String): Boolean {
        return wallet?.checkWalletPassword(password) ?: false
    }

}