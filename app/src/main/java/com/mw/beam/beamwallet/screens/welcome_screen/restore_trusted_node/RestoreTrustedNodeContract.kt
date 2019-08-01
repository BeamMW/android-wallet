package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface RestoreTrustedNodeContract {
    interface View: MvpView
    interface Presenter: MvpPresenter<View>
    interface Repository: MvpRepository
}