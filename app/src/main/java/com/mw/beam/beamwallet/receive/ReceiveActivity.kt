package com.mw.beam.beamwallet.receive

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import kotlinx.android.synthetic.main.activity_receive.*

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveActivity : BaseActivity<ReceivePresenter>(), ReceiveContract.View {
    private lateinit var presenter: ReceivePresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_receive
    override fun getToolbarTitle(): String? = getString(R.string.receive_title)

    override fun addListeners() {
        btnNext.setOnClickListener { presenter.onNextPressed() }
    }

    override fun showToken(token2: String) {
        token.setText(token2)
    }

    override fun getComment(): String? = comment.text?.toString()

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = ReceivePresenter(this, ReceiveRepository())
        return presenter
    }
}
