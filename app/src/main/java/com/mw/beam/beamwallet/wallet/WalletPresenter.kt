package com.mw.beam.beamwallet.wallet

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
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
        }

        txStatusSubscription = repository.getTxStatus().subscribe {
            view?.configTxStatus(it)
        }

        txPeerUpdatedSubscription = repository.getTxPeerUpdated().subscribe {
            if (it != null) {
                view?.configTxPeerUpdated(it)
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
