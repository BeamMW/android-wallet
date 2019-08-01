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

import android.net.Uri
import com.journeyapps.barcodescanner.BarcodeResult
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.PermissionStatus

/**
 * Created by vain onnellinen on 3/15/19.
 */
interface ScanQrContract {
    interface View : MvpView {
        fun pickImageFromGallery()
        fun showPermissionRequiredAlert()
        fun readQrCode(uri: Uri?): BarcodeResult?
        fun finishWithResult(barcodeResult: BarcodeResult)
        fun showNotFoundQrCodeError()
    }

    interface Presenter : MvpPresenter<View> {
        fun onQrFromGalleryPressed()
        fun onRequestPermissionsResult(status: PermissionStatus)
        fun onImageSelected(uri: Uri?)
    }

    interface Repository : MvpRepository
}
