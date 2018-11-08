package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Wallet

/**
 * Created by vain onnellinen on 10/2/18.
 */
interface UtxoContract {
    interface View : MvpView {
        fun configData(wallet: Wallet)
    }

    interface Presenter : MvpPresenter<View>
    interface Repository : MvpRepository
}
