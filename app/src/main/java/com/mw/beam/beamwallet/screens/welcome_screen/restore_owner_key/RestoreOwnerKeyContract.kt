package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface RestoreOwnerKeyContract {
    interface View: MvpView
    interface Presenter: MvpPresenter<View>
    interface Repository: MvpRepository
}