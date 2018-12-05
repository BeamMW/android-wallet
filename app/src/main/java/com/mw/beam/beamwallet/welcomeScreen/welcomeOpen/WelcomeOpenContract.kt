package com.mw.beam.beamwallet.welcomeScreen.welcomeOpen

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 10/19/18.
 */
interface WelcomeOpenContract {
    interface View : MvpView {
        fun hasValidPass(): Boolean
        fun getPass(): String
        fun openWallet()
        fun showChangeAlert()
        fun showOpenWalletError()
        fun clearError()
    }

    interface Presenter : MvpPresenter<View> {
        fun onOpenWallet()
        fun onChangeWallet()
        fun onChangeConfirm()
        fun onPassChanged()
    }

    interface Repository : MvpRepository {
        fun openWallet(pass: String?): AppConfig.Status
    }
}
