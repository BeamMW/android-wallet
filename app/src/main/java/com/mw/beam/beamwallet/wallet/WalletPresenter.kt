package com.mw.beam.beamwallet.wallet

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
class WalletPresenter(currentView: WalletContract.View, private val repository: WalletContract.Repository)
    : BasePresenter<WalletContract.View>(currentView),
        WalletContract.Presenter {
    private lateinit var walletStatusSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable
    private lateinit var txPeerUpdatedSubscription: Disposable
    private lateinit var utxoUpdatedSubscription: Disposable

    //TODO crete State and safe all data there
    var height: Long? = null
    var maturing: Long? = null
    var receiving: Long? = null
    var sending: Long? = null

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

    override fun onSearchPressed() = toDo()
    override fun onFilterPressed() = toDo()
    override fun onExportPressed() = toDo()
    override fun onDeletePressed() = toDo()

    private fun initSubscriptions() {
        walletStatusSubscription = repository.getWalletStatus().subscribe {
            view?.configWalletStatus(it)
            height = it.system.height
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            view?.configTxStatus(data)
            receiving = data.tx?.filter { it.statusEnum == TxStatus.InProgress && it.senderEnum == TxSender.RECEIVED }?.sumByLong { it.amount }
            sending = data.tx?.filter { it.statusEnum == TxStatus.InProgress && it.senderEnum == TxSender.SENT }?.sumByLong { it.amount }
            view?.configInProgress(receiving ?: 0, sending ?: 0, maturing ?: 0)
        }

        txPeerUpdatedSubscription = repository.getTxPeerUpdated().subscribe {
            if (it != null) {
                view?.configTxPeerUpdated(it)
            }
        }

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            if (height != null) {
                maturing = utxos.filter {
                    it.statusEnum == UtxoStatus.Unconfirmed ||
                            (it.statusEnum == UtxoStatus.Unspent && it.maturity > height!!)
                }.sumByLong { it.amount }
                view?.configInProgress(receiving ?: 0, sending ?: 0, maturing ?: 0)
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
