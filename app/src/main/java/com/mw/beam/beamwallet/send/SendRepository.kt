package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.BaseRepository

/**
 * Created by vain onnellinen on 11/13/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SendRepository : BaseRepository(), SendContract.Repository {

    override fun sendMoney(token: String, comment: String?, amount: Long, fee: Long) {
        getResult({ wallet?.sendMoney(token, comment, amount, fee) }, object {}.javaClass.enclosingMethod.name)
    }
}
