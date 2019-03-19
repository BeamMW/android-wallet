package com.mw.beam.beamwallet.screens.qr_scann

import com.journeyapps.barcodescanner.BarcodeCallback
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface QrScannerContract {
    interface View : MvpView {
        fun init(barcodeCallback: BarcodeCallback)
        fun close(result: String)
    }

    interface Presenter: MvpPresenter<View>

    interface Repository: MvpRepository
}