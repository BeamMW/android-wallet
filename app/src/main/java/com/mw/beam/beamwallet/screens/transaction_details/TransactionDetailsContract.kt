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

import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TxStatus
import io.reactivex.Observable
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/18/18.
 */
interface TransactionDetailsContract {
    interface View : MvpView {
        fun getTransactionId(): String
        fun init(txDescription: TxDescription, isEnablePrivacyMode: Boolean)
        fun updatePaymentProof(paymentProof: PaymentProof)
        fun configMenuItems(menu: Menu?, inflater: MenuInflater, txStatus: TxStatus, isSend: Boolean)
        fun finishScreen()
        fun updateUtxos(utxoInfoList: List<UtxoInfoItem>, isEnablePrivacyMode: Boolean)
        fun showCopiedAlert()
        fun showPaymentProof(paymentProof: PaymentProof)
        fun showOpenLinkAlert()
        fun configCategoryAddresses(senderTags: List<Tag>, receiverTags: List<Tag>)
        fun showSendFragment(address: String, amount: Long)
        fun showReceiveFragment(amount: Long, walletAddress: WalletAddress?)
        fun showDeleteSnackBar(txDescription: TxDescription)
        fun configSenderAddressInfo(walletAddress: WalletAddress?)
        fun configReceiverAddressInfo(walletAddress: WalletAddress?)
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
    }

    interface Repository : MvpRepository {
        fun deleteTransaction(txDescription: TxDescription?)
        fun cancelTransaction(txDescription: TxDescription?)
        fun getTxStatus(): Observable<OnTxStatusData>
        fun getAddresses(): Subject<OnAddressesData>
        fun getPaymentProof(txId: String, canRequestProof: Boolean): Subject<PaymentProof>
        fun getUtxoByTx(txId: String): Subject<List<Utxo>?>
        fun requestProof(txId: String)
        fun isAllowOpenExternalLink(): Boolean
        fun getAddressTags(address: String): List<Tag>
    }
}
