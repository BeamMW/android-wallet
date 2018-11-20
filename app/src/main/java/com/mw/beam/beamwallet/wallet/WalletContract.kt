package com.mw.beam.beamwallet.wallet

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.*
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface WalletContract {
    interface View : MvpView {
        fun init()
        fun configWalletStatus(walletStatus: WalletStatus)
        fun configTxStatus(txStatusData: OnTxStatusData)
        fun configTxPeerUpdated(peers: Array<TxPeer>)
        fun configInProgress(receivingAmount : Long, sendingAmount: Long, maturingAmount : Long)
        fun showTransactionDetails(txDescription: TxDescription)
        fun showReceiveScreen()
        fun showSendScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun onReceivePressed()
        fun onSendPressed()
        fun onTransactionPressed(txDescription: TxDescription)
        fun onSearchPressed()
        fun onFilterPressed()
        fun onExportPressed()
        fun onDeletePressed()
    }

    interface Repository : MvpRepository {
        fun getWalletStatus(): Subject<WalletStatus>
        fun getTxStatus(): Subject<OnTxStatusData>
        fun getTxPeerUpdated(): Subject<Array<TxPeer>?>
        fun getUtxoUpdated(): Subject<Array<Utxo>>
    }
}
