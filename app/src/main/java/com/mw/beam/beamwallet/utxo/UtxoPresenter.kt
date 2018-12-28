package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.Utxo
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoPresenter(currentView: UtxoContract.View, private val repository: UtxoContract.Repository)
    : BasePresenter<UtxoContract.View>(currentView),
        UtxoContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onUtxoPressed(utxo: Utxo) {
        view?.showUtxoDetails(utxo)
    }

    private fun initSubscriptions() {
        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            view?.updateUtxos(utxos)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        initSubscriptions()
        return arrayOf(utxoUpdatedSubscription)
    }

    override fun hasBackArrow(): Boolean? = null
}
