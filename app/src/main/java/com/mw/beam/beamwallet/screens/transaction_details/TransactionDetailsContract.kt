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

package com.mw.beam.beamwallet.screens.transaction_details

import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.*
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import java.io.File

/**
 *  10/18/18.
 */
interface TransactionDetailsContract {
    interface View : MvpView {
        fun getTransactionId(): String
        fun init(txDescription: TxDescription, isEnablePrivacyMode: Boolean)
        fun updateAddresses(txDescription: TxDescription)
        fun updatePaymentProof(paymentProof: PaymentProof)
        fun configMenuItems(menu: Menu?, inflater: MenuInflater, transaction: TxDescription?)
        fun finishScreen()
        fun updateUtxos(utxoInfoList: List<UtxoInfoItem>, isEnablePrivacyMode: Boolean)
        fun showCopiedAlert()
        fun showPaymentProof(paymentProof: PaymentProof)
        fun showOpenLinkAlert()
        fun showSendFragment(address: String, amount: Long)
        fun showSaveContact(address: String?)
        fun showReceiveFragment(amount: Long, walletAddress: WalletAddress?)
        fun showDeleteSnackBar(txDescription: TxDescription)
        fun convertViewIntoBitmap(): Bitmap?
        fun shareTransactionDetails(file: File?)
        fun handleExpandDetails(shouldExpandDetails: Boolean)
        fun handleExpandUtxos(shouldExpandUtxos: Boolean)
        fun handleExpandProof(shouldExpandProof: Boolean)
        fun showCancelAlert()
        fun showDeleteAlert()
        fun copyDetails()
    }

    interface Presenter : MvpPresenter<View> {
        fun onMenuCreate(menu: Menu?, inflater: MenuInflater)
        fun onCancelTransaction()
        fun onDeleteTransaction()
        fun onShowPaymentProof()
        fun onCopyPaymentProof()
        fun onOpenInBlockExplorerPressed()
        fun onOpenLinkPressed()
        fun onRepeatTransaction()
        fun onSharePressed()
        fun onExpandDetailedPressed()
        fun onExpandUtxosPressed()
        fun onExpandProofPressed()
        fun onSaveContact()
        fun onCancelTransactionConfirm()
        fun onDeleteTransactionsPressed()
        fun onCopyDetailsPressed()
    }

    interface Repository : MvpRepository {
        fun deleteTransaction(txDescription: TxDescription?)
        fun cancelTransaction(txDescription: TxDescription?)
        fun getTxStatus(): Observable<OnTxStatusData>
        fun getPaymentProof(txId: String, canRequestProof: Boolean): Subject<PaymentProof>
        fun getUtxoByTx(txId: String): Subject<List<Utxo>?>
        fun requestProof(txId: String)
        fun isAllowOpenExternalLink(): Boolean
        fun saveImage(bitmap: Bitmap?): File?
    }
}
