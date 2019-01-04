package com.mw.beam.beamwallet.wallet

import android.view.MenuItem
import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.entities.WalletStatus
import io.reactivex.subjects.Subject
import android.view.View as MenuView

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface WalletContract {
    interface View : MvpView {
        fun init()
        fun configWalletStatus(walletStatus: WalletStatus)
        fun configTransactions(transactions: List<TxDescription>)
        fun configInProgress(receivingAmount: Long, sendingAmount: Long, maturingAmount: Long)
        fun configAvailable(availableAmount: Long)
        fun showTransactionDetails(txDescription: TxDescription)
        fun showReceiveScreen()
        fun showSendScreen()
        fun handleExpandAvailable(shouldExpandAvailable: Boolean)
        fun handleExpandInProgress(shouldExpandInProgress: Boolean)
        fun handleTransactionsMenu(item: MenuItem): Boolean
        fun showTransactionsMenu(menu: MenuView)
    }

    interface Presenter : MvpPresenter<View> {
        fun onReceivePressed()
        fun onSendPressed()
        fun onTransactionPressed(txDescription: TxDescription)
        fun onSearchPressed()
        fun onFilterPressed()
        fun onExportPressed()
        fun onDeletePressed()
        fun onExpandAvailablePressed()
        fun onExpandInProgressPressed()
        fun onTransactionsMenuPressed(item: MenuItem): Boolean
        fun onTransactionsMenuButtonPressed(menu: MenuView)
    }

    interface Repository : MvpRepository {
        fun getWalletStatus(): Subject<WalletStatus>
        fun getTxStatus(): Subject<OnTxStatusData>
        fun getUtxoUpdated(): Subject<List<Utxo>>
    }
}
