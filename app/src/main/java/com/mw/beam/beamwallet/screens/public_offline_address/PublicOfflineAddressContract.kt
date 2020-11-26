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

package com.mw.beam.beamwallet.screens.public_offline_address

import android.graphics.Bitmap
import android.view.Menu
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import java.io.File

/**
 *  3/4/19.
 */
interface PublicOfflineAddressContract {
    interface View : MvpView {
        fun init(token: String)
        fun shareQR(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSharePressed(image:Bitmap)
        fun onCopyToken()
    }

    interface Repository : MvpRepository {
        fun saveImage(bitmap: Bitmap): File
    }
}
