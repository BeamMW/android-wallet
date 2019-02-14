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

package com.mw.beam.beamwallet.screens.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.core.views.BeamButton
import com.mw.beam.beamwallet.core.watchers.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_receive.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveActivity : BaseActivity<ReceivePresenter>(), ReceiveContract.View {
    private lateinit var presenter: ReceivePresenter
    private var dialog: AlertDialog? = null
    private val COPY_TAG = "TOKEN"
    private val QR_SIZE = 160.0
    private val expireListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            presenter.onExpirePeriodChanged(when (position) {
                ExpirePeriod.DAY.ordinal -> ExpirePeriod.DAY
                else -> ExpirePeriod.NEVER
            })
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_receive
    override fun getToolbarTitle(): String? = getString(R.string.receive_title)

    override fun init() {
        ArrayAdapter.createFromResource(
                this,
                R.array.receive_expires_periods,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            expiresOnSpinner.adapter = adapter
            expiresOnSpinner.setSelection(0)
        }
    }

    override fun addListeners() {
        btnCopyToken.setOnClickListener { presenter.onCopyTokenPressed() }
        btnShowQR.setOnClickListener { presenter.onShowQrPressed() }
        expiresOnSpinner.onItemSelectedListener = expireListener
    }

    override fun showToken(receiveToken: String) {
        token.text = receiveToken
    }

    override fun showQR(receiveToken: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_receive, null)
        val qrView = view.findViewById<ImageView>(R.id.qrView)
        val token = view.findViewById<TextView>(R.id.tokenView)
        val btnCopy = view.findViewById<BeamButton>(R.id.btnCopy)
        val close = view.findViewById<ImageView>(R.id.close)

        token.text = receiveToken

        try {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val logicalDensity = metrics.density
            val px = Math.ceil(QR_SIZE * logicalDensity).toInt()

            qrView.setImageBitmap(QrHelper.textToImage(receiveToken, px, px,
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    ContextCompat.getColor(this, R.color.common_text_color)))
        } catch (e: Exception) {
            return
        }

        btnCopy.setOnClickListener { presenter.onDialogCopyPressed() }
        close.setOnClickListener { presenter.onDialogClosePressed() }

        dialog = AlertDialog.Builder(this).setView(view).show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun getComment(): String? = comment.text?.toString()

    override fun copyToClipboard(receiveToken: String) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(COPY_TAG, receiveToken)
    }

    override fun close() {
        finish()
    }

    override fun dismissDialog() {
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
        super.onBackPressed()
    }

    override fun clearListeners() {
        btnCopyToken.setOnClickListener(null)
        btnShowQR.setOnClickListener(null)
        expiresOnSpinner.onItemSelectedListener = null
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = ReceivePresenter(this, ReceiveRepository(), ReceiveState())
        return presenter
    }
}
