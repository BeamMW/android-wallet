package com.mw.beam.beamwallet.welcomeScreen.welcomeCreate

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 12/4/18.
 */
interface WelcomeCreateContract {
    interface View : MvpView {
        fun createWallet()
        fun restoreWallet()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCreateWallet()
        fun onRestoreWallet()
    }

    interface Repository : MvpRepository
}
