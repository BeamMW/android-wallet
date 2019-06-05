package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BaseRepository

class AppActivityRepository: BaseRepository(), AppActivityContract.Repository {
    override fun sendMoney(token: String, comment: String?, amount: Long, fee: Long) {
        getResult("sendMoney", " token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {
            wallet?.sendMoney(token, comment, amount, fee)
        }
    }
}