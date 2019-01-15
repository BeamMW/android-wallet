package com.mw.beam.beamwallet.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.activity_receive.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveActivity : BaseActivity<ReceivePresenter>(), ReceiveContract.View {
    private lateinit var presenter: ReceivePresenter
    private val COPY_TAG = "TOKEN"

    override fun onControllerGetContentLayoutId() = R.layout.activity_receive
    override fun getToolbarTitle(): String? = getString(R.string.receive_title)

    override fun addListeners() {
        btnCopyToken.setOnClickListener { presenter.onCopyTokenPressed() }
        btnShowQR.setOnClickListener { presenter.onShowQrPressed() }
    }

    override fun showToken(receiveToken: String) {
        token.text = receiveToken
    }

    override fun getComment(): String? = comment.text?.toString()

    override fun copyToClipboard(receiveToken: String) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(COPY_TAG, receiveToken)
    }

    override fun close() {
        finish()
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
        super.onBackPressed()
    }

    override fun clearListeners() {
        btnCopyToken.setOnClickListener(null)
        btnShowQR.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = ReceivePresenter(this, ReceiveRepository(), ReceiveState())
        return presenter
    }
}
