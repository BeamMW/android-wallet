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

package com.mw.beam.beamwallet.screens.qr_dialog

import android.graphics.Bitmap
import android.net.Uri
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import java.io.File

interface QrDialogContract {
    interface View: MvpView {
        fun getWalletAddress(): WalletAddress
        fun getAmount(): Long
        fun init(walletAddress: WalletAddress, amount: Long)
        fun shareQR(file: File)
    }

    interface Presenter: MvpPresenter<View> {
        fun onSharePressed(bitmap: Bitmap)
    }

    interface Repository: MvpRepository {
        fun saveImage(bitmap: Bitmap): File
    }
}