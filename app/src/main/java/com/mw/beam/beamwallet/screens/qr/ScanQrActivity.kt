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

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import kotlinx.android.synthetic.main.activity_scan_qr.*
import android.content.Intent
import android.graphics.ImageFormat
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.SourceData


/**
 * Created by vain onnellinen on 3/15/19.
 */
class ScanQrActivity : BaseActivity<ScanQrPresenter>(), ScanQrContract.View, DecoratedBarcodeView.TorchListener {
    private val permissionRequestCode = 5421
    private val pickImageRequestCode = 4312

    override fun onControllerGetContentLayoutId() = R.layout.activity_scan_qr
    override fun getToolbarTitle(): String? = getString(R.string.scan_qr_code)

    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        capture = CaptureManager(this, barcodeView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        barcodeView.setTorchListener(this)
        (barcodeView.findViewById<View>(R.id.zxing_status_view)).visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scan_qr_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.qrFromGallery) {
            presenter?.onQrFromGalleryPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun pickImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionRequestCode)
        } else {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPickture)), pickImageRequestCode)
        }
    }

    override fun showPermissionRequiredAlert() {
        showAlert(message = getString(R.string.storage_permission_required_message),
                btnConfirmText = getString(R.string.settings),
                onConfirm = { showAppDetailsPage() },
                title = getString(R.string.send_permission_required_title),
                btnCancelText = getString(R.string.cancel))
    }

    private fun showAppDetailsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImageRequestCode) {
            data?.let {
                presenter?.onImageSelected(it.data)
            }
        }
    }

    override fun readQrCode(uri: Uri?): BarcodeResult? {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri) ?: return null

        val intArray = IntArray(bitmap.width * bitmap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val byteArray = intArray.foldIndexed(ByteArray(intArray.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }

        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val sourceData = SourceData(byteArray, bitmap.width, bitmap.height, ImageFormat.JPEG, 0)

        return try {
            BarcodeResult(MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source))), sourceData)
        } catch (e: Exception) {
            null
        }
    }

    override fun finishWithResult(barcodeResult: BarcodeResult) {
        val intent = CaptureManager.resultIntent(barcodeResult, null)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun showNotFoundQrCodeError() {
        showSnackBar(getString(R.string.qr_code_cannot_be_recognized_please_try_another_picture))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var isGranted = true

        for ((index, permission) in permissions.withIndex()) {
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                isGranted = false
                if (!shouldShowRequestPermissionRationale(permission)) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.NEVER_ASK_AGAIN,requestCode)
                }
                else if (Manifest.permission.READ_EXTERNAL_STORAGE == permission || Manifest.permission.WRITE_EXTERNAL_STORAGE == permission) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.DECLINED, requestCode)
                }
            }
        }

        if (isGranted) {
            presenter?.onRequestPermissionsResult(PermissionStatus.GRANTED, requestCode)
        }
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
