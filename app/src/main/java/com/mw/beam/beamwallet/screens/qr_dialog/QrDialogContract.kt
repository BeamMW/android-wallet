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