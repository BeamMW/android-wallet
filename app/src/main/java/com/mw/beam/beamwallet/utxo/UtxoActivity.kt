package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Wallet

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoActivity : BaseActivity<UtxoPresenter>(), UtxoContract.View {
    private lateinit var presenter: UtxoPresenter

    override fun onControllerGetContentLayoutId() = R.layout.activity_utxo
    override fun getToolbarTitle(): String? = null

    override fun configData(wallet: Wallet) {
        // height.text = wallet.getSystemState().height.toString()
        //  hash.text = wallet.getSystemState().hash.toString()
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = UtxoPresenter(this, UtxoRepository())
        return presenter
    }
}
