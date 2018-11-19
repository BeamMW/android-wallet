package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun init()
        fun getAmount(): Long
        fun getFee(): Long
        fun getToken(): String
        fun getComment(): String?
    }

    interface Presenter : MvpPresenter<View> {
        fun onSend()
    }

    interface Repository : MvpRepository {
        fun sendMoney(token: String, comment: String?, amount: Long, fee: Long)
    }
}
