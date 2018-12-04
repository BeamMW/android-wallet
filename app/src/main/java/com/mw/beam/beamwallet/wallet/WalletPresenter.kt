package com.mw.beam.beamwallet.wallet

import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.sumByLong
import io.reactivex.disposables.Disposable


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletPresenter(currentView: WalletContract.View, private val repository: WalletContract.Repository, private val state: WalletState)
    : BasePresenter<WalletContract.View>(currentView),
        WalletContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable
    private lateinit var txPeerUpdatedSubscription: Disposable
    private lateinit var utxoUpdatedSubscription: Disposable

    override fun onStart() {
        super.onStart()
        view?.init()
    }

    override fun onReceivePressed() {
        view?.showReceiveScreen()
    }

    override fun onSendPressed() {
        view?.showSendScreen()
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription)
    }

    override fun onExpandAvailablePressed() {
        state.shouldExpandAvailable = !state.shouldExpandAvailable
        view?.handleExpandAvailable(state.shouldExpandAvailable)
    }

    override fun onExpandInProgressPressed() {
        state.shouldExpandInProgress = !state.shouldExpandInProgress
        view?.handleExpandInProgress(state.shouldExpandInProgress)

        if (!state.shouldExpandInProgress) {
            view?.configInProgress(state.receiving, state.sending, state.maturing)
        }
    }

    override fun onTransactionsMenuButtonPressed(menu: View) {
        view?.showTransactionsMenu(menu)
    }

    override fun onTransactionsMenuPressed(item: MenuItem): Boolean {
        return view?.handleTransactionsMenu(item) ?: false
    }

    override fun onSearchPressed() = toDo()
    override fun onFilterPressed() = toDo()
    override fun onExportPressed() = toDo()
    override fun onDeletePressed() = toDo()

    private fun initSubscriptions() {
        walletStatusSubscription = repository.getWalletStatus().subscribe {
            view?.configWalletStatus(it)
            state.height = it.system.height
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            view?.configTxStatus(data)
            state.receiving = data.tx?.filter { it.statusEnum == TxStatus.InProgress && it.senderEnum == TxSender.RECEIVED }?.sumByLong { it.amount } ?: 0
            state.sending = data.tx?.filter { it.statusEnum == TxStatus.InProgress && it.senderEnum == TxSender.SENT }?.sumByLong { it.amount } ?: 0
            view?.configInProgress(state.receiving, state.sending, state.maturing)
        }

        txPeerUpdatedSubscription = repository.getTxPeerUpdated().subscribe {
            if (it != null) {
                view?.configTxPeerUpdated(it)
            }
        }

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            if (state.height != null) {
                state.maturing = utxos?.filter {
                    it.statusEnum == UtxoStatus.Unconfirmed ||
                            (it.statusEnum == UtxoStatus.Unspent && it.maturity > state.height!!)
                }?.sumByLong { it.amount } ?: 0
                view?.configInProgress(state.receiving, state.sending, state.maturing)
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        initSubscriptions()
        return arrayOf(walletStatusSubscription, txStatusSubscription, txPeerUpdatedSubscription)
    }

    private fun toDo() {
        view?.showSnackBar("Coming soon...")
    }
}
