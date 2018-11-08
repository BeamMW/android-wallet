package com.mw.beam.beamwallet.utxo

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.core.entities.Wallet

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoActivity : BaseActivity<UtxoPresenter>(), UtxoContract.View {
    private lateinit var presenter: UtxoPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utxo)

        presenter = UtxoPresenter(this, UtxoRepository())
        configPresenter(presenter)
    }

    override fun configData(wallet: Wallet) {
       // height.text = wallet.getSystemState().height.toString()
      //  hash.text = wallet.getSystemState().hash.toString()
    }
}
