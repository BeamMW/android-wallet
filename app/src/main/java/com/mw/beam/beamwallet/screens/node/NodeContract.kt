package com.mw.beam.beamwallet.screens.node

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface NodeContract {

    interface View: MvpView {
    }

    interface Presenter: MvpPresenter<View> {
    }

    interface Repository: MvpRepository {
    }
}