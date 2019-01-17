package com.mw.beam.beamwallet.send

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun getAmount(): Double
        fun getFee(): Long
        fun getToken(): String
        fun getComment(): String?
        fun updateUI(shouldShowParams: Boolean)
        fun hasErrors() : Boolean
        fun clearErrors()
        fun init()
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSend()
        fun onTokenChanged(isTokenEmpty: Boolean)
        fun onAmountChanged()
    }

    interface Repository : MvpRepository {
        fun sendMoney(token: String, comment: String?, amount: Long, fee: Long)
    }
}
