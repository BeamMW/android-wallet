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

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseDialogFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import kotlinx.android.synthetic.main.dialog_qr_code.*
import java.io.File

class QrDialogFragment: BaseDialogFragment<QrDialogPresenter>(), QrDialogContract.View {

    companion object {
        private const val QR_SIZE = 160.0
    }

    private val args by lazy {
        QrDialogFragmentArgs.fromBundle(arguments!!)
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.dialog_qr_code

    override fun getWalletAddress(): WalletAddress = args.walletAddress

    override fun getAmount(): Long = args.amount

    override fun getReceiveFromWallet(): Boolean = args.receiveFromWallet

    override fun getToken(): String? = args.token

    override fun getIsOldDesign(): Boolean = args.isOldDesign

    @SuppressLint("SetTextI18n")
    override fun init(walletAddress: WalletAddress, amount: Long) {
        hideKeyboard()

        var token = getToken()
        val qrImage: Bitmap?

        if(token != null) {
            if(token == "null" && getIsOldDesign()) {
                token = walletAddress.walletID
            }
           if (!getIsOldDesign()) {
               tokenTitle.visibility = View.GONE
               tokenView.visibility = View.GONE
               amountTitle.visibility = View.GONE
               amountView.visibility = View.GONE
               secondAvailableSum.visibility = View.GONE
               infoLabel.text = resources.getString(R.string.receive_description)
           }
            else {
               amountTitle.visibility = View.GONE
               amountView.visibility = View.GONE
               secondAvailableSum.visibility = View.GONE


               tokenView.text = token
               infoLabel.text = resources.getString(R.string.receive_description_qr)
           }

            try {
                val metrics = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
                val logicalDensity = metrics.density
                val px = Math.ceil(QR_SIZE * logicalDensity).toInt()

                qrImage = QrHelper.textToImage(QrHelper.createQrString(token, null), px, px,
                        ContextCompat.getColor(context!!, R.color.common_text_color),
                        ContextCompat.getColor(context!!, R.color.colorPrimary))

                qrView.setImageBitmap(qrImage)
            } catch (e: Exception) {
                return
            }
        }
        else {
            val receiveToken = if(getReceiveFromWallet()) {
                walletAddress.token.trimAddress()
            }
            else {
                walletAddress.walletID.trimAddress()
            }

            tokenView.text = receiveToken

            try {
                val metrics = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
                val logicalDensity = metrics.density
                val px = Math.ceil(QR_SIZE * logicalDensity).toInt()

                qrImage = QrHelper.textToImage(QrHelper.createQrString(receiveToken, amount.convertToBeam()), px, px,
                        ContextCompat.getColor(context!!, R.color.common_text_color),
                        ContextCompat.getColor(context!!, R.color.colorPrimary))

                qrView.setImageBitmap(qrImage)
            } catch (e: Exception) {
                return
            }

            val amountVisibility = if (amount > 0) View.VISIBLE else View.GONE
            amountTitle.visibility = amountVisibility
            amountView.visibility = amountVisibility
            secondAvailableSum.visibility = amountVisibility

            amountView.text = "${amount.convertToBeamString()} ${getString(R.string.currency_beam)}".toUpperCase()
            secondAvailableSum.text = amount.convertToCurrencyString()
        }

        btnShare.setOnClickListener { presenter?.onSharePressed(qrImage!!) }
        close.setOnClickListener { findNavController().popBackStack() }
    }

    override fun shareQR(file: File) {
        val uri = FileProvider.getUriForFile(context!!, AppConfig.AUTHORITY, file)

        context?.apply {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
            }

            startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
        }
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return QrDialogPresenter(this, QrDialogRepository(), QrDialogState())
    }
}