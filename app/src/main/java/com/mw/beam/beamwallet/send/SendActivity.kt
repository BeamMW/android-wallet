package com.mw.beam.beamwallet.send

import android.support.v7.widget.Toolbar
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.activity_send.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendActivity : BaseActivity<SendPresenter>(), SendContract.View {
    private lateinit var presenter: SendPresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_send

    override fun init() {
        val toolbar = toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        initToolbar(toolbar, getString(R.string.send_title), true)

    }

    override fun getAmount(): Long {
        //TODO handle NumberFormatException or show error
        return amount.text.toString().toLong()
    }

    override fun getFee(): Long {
        return fee.text.toString().toLong()
    }

    override fun getToken(): String {
        return token.text.toString()
    }

    override fun getComment(): String? {
        return comment.text.toString()
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter.onSend()
        }
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = SendPresenter(this, SendRepository())
        return presenter
    }
}
