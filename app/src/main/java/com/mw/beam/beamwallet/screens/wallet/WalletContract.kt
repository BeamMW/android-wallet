/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.wallet

import android.view.MenuItem
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import io.reactivex.subjects.Subject
import java.io.File
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
        fun showShareFileChooser(file: File)
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
        fun getTransactionsFile(): File
    }
}
