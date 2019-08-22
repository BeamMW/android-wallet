package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface RestoreTrustedNodeContract {

    interface View: MvpView {
        fun init()
        fun getNodeAddress(): String
        fun showLoading()
        fun dismissLoading()
        fun showError()
        fun navigateToProgress()
    }

    interface Presenter: MvpPresenter<View> {
        fun onNextPressed()
    }

    interface Repository : MvpRepository {
        fun connectToNode(address: String)
    }
}