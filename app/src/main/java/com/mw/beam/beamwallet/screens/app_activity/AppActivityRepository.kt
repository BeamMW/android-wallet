package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BaseRepository

class AppActivityRepository: BaseRepository(), AppActivityContract.Repository {
    override fun sendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        getResult("sendMoney", " sender: $outgoingAddress\n token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {
            wallet?.sendMoney(outgoingAddress, token, comment, amount, fee)
        }
    }
}