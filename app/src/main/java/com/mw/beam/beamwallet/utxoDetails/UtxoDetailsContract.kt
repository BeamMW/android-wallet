package com.mw.beam.beamwallet.utxoDetails

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Utxo

/**
 * Created by vain onnellinen on 12/20/18.
 */
interface UtxoDetailsContract {
    interface View : MvpView {
        fun getUtxoDetails(): Utxo
        fun init(utxo: Utxo)
    }

    interface Presenter : MvpPresenter<View>

    interface Repository : MvpRepository {
        var utxo: Utxo?
    }
}
