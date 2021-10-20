package com.mw.beam.beamwallet.screens.apps.confirm

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface AppConfirmContract {
    interface View: MvpView {
        fun init()
    }

    interface Presenter: MvpPresenter<View> {
    }

    interface Repository: MvpRepository {

    }
}