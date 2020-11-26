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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.screens.qr_dialog.QrDialogFragment

import kotlinx.android.synthetic.main.fragment_public_offline_address.*
import kotlinx.android.synthetic.main.fragment_public_offline_address.btnCopy
import kotlinx.android.synthetic.main.fragment_public_offline_address.btnShare
import kotlinx.android.synthetic.main.fragment_public_offline_address.qrView
import java.io.File
import kotlin.math.ceil

/**
 *  3/4/19.
 */
class PublicOfflineAddressFragment : BaseFragment<PublicOfflineAddressPresenter>(), PublicOfflineAddressContract.View {
    override fun onControllerGetContentLayoutId() = R.layout.fragment_public_offline_address
    override fun getToolbarTitle(): String? = getString(R.string.public_offline_address)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.hasStatus = true
    }

    @SuppressLint("SetTextI18n")
    override fun init(token: String) {

        addressValue.text = token

        try {
            val metrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
            val logicalDensity = metrics.density
            val px = ceil(QrDialogFragment.QR_SIZE * logicalDensity).toInt()

            val qrImage = QrHelper.textToImage(QrHelper.createQrString(token, null), px, px,
                    ContextCompat.getColor(requireContext(), R.color.common_text_color),
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary))

            qrView.setImageBitmap(qrImage)

            btnShare.setOnClickListener {
                presenter?.onSharePressed(qrImage!!) }

        } catch (e: Exception) {
            return
        }
    }

    override fun addListeners() {
        btnCopy.setOnClickListener {
            presenter?.onCopyToken()
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }
    }

    override fun shareQR(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), AppConfig.AUTHORITY, file)

        context?.apply {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
            }

            startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
        }
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return PublicOfflineAddressPresenter(this, PublicOfflineAddressRepository(), PublicOfflineAddressState())
    }

    override fun clearListeners() {
        btnCopy.setOnClickListener(null)
    }
}
