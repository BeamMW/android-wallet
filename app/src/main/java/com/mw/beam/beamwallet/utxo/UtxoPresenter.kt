package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoPresenter(currentView: UtxoContract.View, private val repository: UtxoContract.Repository, private val state: UtxoState)
    : BasePresenter<UtxoContract.View>(currentView),
        UtxoContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onUtxoPressed(utxo: Utxo) {
        view?.showUtxoDetails(utxo, state.configTransactions().filter { it.id == utxo.createTxId || it.id == utxo.spentTxId } as ArrayList<TxDescription>)
    }

    private fun initSubscriptions() {
        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            view?.updateUtxos(utxos)
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            state.configTransactions(data.tx)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        initSubscriptions()
        return arrayOf(utxoUpdatedSubscription, txStatusSubscription)
    }

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
