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

package com.mw.beam.beamwallet.screens.transactions

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.utils.TransactionFields
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class TransactionsPresenter(view: TransactionsContract.View?, repository: TransactionsContract.Repository)
    : BasePresenter<TransactionsContract.View, TransactionsContract.Repository>(view, repository), TransactionsContract.Presenter {

    private lateinit var txStatusSubscription: Disposable
    private lateinit var exportCSVSubscription: Disposable

    var removedTransactions = mutableListOf<String>()
    var isAllSelected = false
    var isShare = false

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init()
//
//      android.os.Handler().postDelayed({
//          view?.init()
//          view?.configTransactions(getTransactions())
//      }, 200)

    }

//    override fun onStart() {
//        super.onStart()
//
//        view?.init()
//
//        doAsync {
//            val tr = AppManager.instance.getTransactions().sortedByDescending { it.createTime }
//            uiThread {
//                view?.configTransactions(tr)
//            }
//        }
//    }

    fun getTransactions() = AppManager.instance.getTransactions().sortedByDescending { it.createTime }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

    override fun onSelectAll() {
        isAllSelected = !isAllSelected

        if(isAllSelected)
        {
            view?.didSelectAllTransactions(getTransactions())
        }
        else{
            view?.didUnSelectAllTransactions()
        }
    }

    override fun onSearchPressed() {
        view?.showSearchTransaction()
    }

    override fun onRepeatTransaction() {
        view?.showRepeatTransaction()
    }

    override fun onExportSave() {
        isShare = false
        AppManager.instance.wallet?.exportTxHistoryToCsv()
    }

    override fun onExportShare() {
        isShare = true
        AppManager.instance.wallet?.exportTxHistoryToCsv()
    }

    override fun onDeleteTransactionsPressed() {
        view?.deleteTransactions()
    }

    override fun onConfirmDeleteTransactions(transactions: List<String>) {
        var hasInProgress = false

        removedTransactions.clear()
        removedTransactions.addAll(transactions)

        for (i in 0 until transactions.count()) {
            val id = transactions[i]
            val transaction = AppManager.instance.getTransaction(id)
            if (transaction != null) {
                if(!transaction.isInProgress()) {
                    repository.deleteTransaction(transaction)
                }
                else{
                    hasInProgress = true
                }
            }
        }

        if (hasInProgress) {
            view?.showInProgressToast()
        }
    }

    override fun onProofVerificationPressed() {
        view?.showProofVerification()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.configTransactions(getTransactions())

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            view?.configTransactions(getTransactions())
        }

        txStatusSubscription = AppManager.instance.subOnExportToCSV.subscribe {
            if (!isShare) {
                view?.exportSave(it)
            }
            else {
                val file = repository.getTransactionsFile()
                file.writeBytes(it.toByteArray())
                view?.exportShare(file)
            }
        }
    }

    override fun onModeChanged(mode: TransactionsFragment.Mode) {
        view?.changeMode(mode)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription, txStatusSubscription)

}
