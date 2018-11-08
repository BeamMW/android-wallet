package com.mw.beam.beamwallet.welcomeScreen.welcomeMain

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 10/19/18.
 */
interface WelcomeMainContract {
    interface View : MvpView {
        fun createWallet()
        fun configScreen(isWalletInitialized: Boolean)
        fun hasValidPass(): Boolean
        fun getPass(): String
        fun openWallet()
        fun showChangeAlert()
        fun showOpenWalletError()
        fun clearError()
        fun restoreWallet()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCreateWallet()
        fun onRestoreWallet()
        fun onOpenWallet()
        fun onChangeWallet()
        fun onChangeConfirm()
        fun onPassChanged()
    }

    interface Repository : MvpRepository {
        fun isWalletInitialized(): Boolean
        fun openWallet(pass: String?): AppConfig.Status
    }
}
