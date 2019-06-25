package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface SaveAddressContract {
    interface View: MvpView {
        fun getAddress(): String
        fun init(address: String)
        fun close()
    }
    interface Presenter: MvpPresenter<View> {
        fun onSavePressed()
        fun onCancelPressed()
    }
    interface Repository: MvpRepository
}