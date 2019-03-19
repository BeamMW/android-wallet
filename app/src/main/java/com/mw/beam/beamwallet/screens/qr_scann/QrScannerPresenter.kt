package com.mw.beam.beamwallet.screens.qr_scann

import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.mw.beam.beamwallet.base_screen.BasePresenter

class QrScannerPresenter(currentView: QrScannerContract.View, repository: QrScannerContract.Repository)
    : BasePresenter<QrScannerContract.View, QrScannerContract.Repository>(currentView, repository) {

    override fun onViewCreated() {
        view?.init(object: BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                view?.close(result.text)
            }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })
    }
}