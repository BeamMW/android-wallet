package com.mw.beam.beamwallet.screens.owner_key

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface OwnerKeyContract {
    interface View: MvpView {
        fun init(key: String)
        fun showCopiedSnackBar()
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyPressed()
    }

    interface Repository: MvpRepository {
        fun getOwnerKey(): String
    }
}