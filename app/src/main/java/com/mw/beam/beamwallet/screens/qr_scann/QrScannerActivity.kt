package com.mw.beam.beamwallet.screens.qr_scann

import android.Manifest
import android.app.Activity
import android.content.Intent
import com.journeyapps.barcodescanner.BarcodeCallback
import com.mw.beam.beamwallet.R
import kotlinx.android.synthetic.main.activity_qr_scanner.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

class QrScannerActivity : BaseActivity<QrScannerPresenter>(), QrScannerContract.View {
    companion object {
        const val RESULT_KEY = "result"
    }
    lateinit var presenter: QrScannerPresenter

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_qr_scanner

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = QrScannerPresenter(this, QrScannerRepository())
        return presenter
    }

    override fun getToolbarTitle(): String? = getString(R.string.scan_qr_code)

    override fun init(barcodeCallback: BarcodeCallback) {
        requestPermission()
        barcode_scanner.decodeContinuous(barcodeCallback)
        barcode_scanner.decoderFactory = DefaultDecoderFactory(null, null, null, 2)
        initToolbar(getToolbarTitle(), true, hasStatus = false)
    }

    override fun close(result: String) {
        val intent = Intent()
        intent.putExtra(RESULT_KEY, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        resumeScanner()
    }

    private fun resumeScanner() {
        if (!barcode_scanner.isActivated)
            barcode_scanner.resume()
    }

    override fun onPause() {
        super.onPause()
        barcode_scanner.pause()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isEmpty()) {
            requestPermission()
        } else {
            barcode_scanner.resume()
        }
    }
}
