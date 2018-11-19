package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendRepository : BaseRepository(), SendContract.Repository {

    override fun sendMoney(token: String, comment: String?, amount: Long, fee: Long) {
        wallet?.sendMoney(token, comment, amount, fee)
    }
}
