package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.helpers.WelcomeMode

interface RestoreOwnerKeyContract {

    interface View: MvpView {
        fun init(key: String)
        fun getPassword(): String
        fun getSeed(): Array<String>
        fun showCopiedSnackBar()
        fun navigateToEnterTrustedNode()
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyPressed()
        fun onNextPressed()
    }

    interface Repository: MvpRepository {
        fun getOwnerKey(pass: String): String
        fun createWallet(pass: String?, seed: String?): Status
    }
}