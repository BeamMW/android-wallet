/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.qr

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import kotlinx.android.synthetic.main.activity_scan_qr.*


/**
 * Created by vain onnellinen on 3/15/19.
 */
class ScanQrActivity : BaseActivity<ScanQrPresenter>(), ScanQrContract.View, DecoratedBarcodeView.TorchListener {

    override fun onControllerGetContentLayoutId() = R.layout.activity_scan_qr
    override fun getToolbarTitle(): String? = getString(R.string.scan_qr_title)

    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        capture = CaptureManager(this, barcodeView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        barcodeView.setTorchListener(this)
        (barcodeView.findViewById<View>(R.id.zxing_status_view)).visibility = View.GONE
    }

    override fun onControllerResume() {
        super.onControllerResume()
        capture.onResume()
    }

    override fun onControllerPause() {
        super.onControllerPause()
        capture.onPause()
    }

    override fun onControllerSaveInstanceState(outState: Bundle) {
        super.onControllerSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onTorchOn() {}

    override fun onTorchOff() {}

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ScanQrPresenter(this, ScanQrRepository())
    }
}
