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
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.utils.TransactionFields
import io.reactivex.disposables.Disposable

class TransactionsPresenter(view: TransactionsContract.View?, repository: TransactionsContract.Repository)
    : BasePresenter<TransactionsContract.View, TransactionsContract.Repository>(view, repository), TransactionsContract.Presenter {
    private lateinit var txStatusSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    private val transactions = HashMap<String, TxDescription>()

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    private fun updateTransactions(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }

        return getTransactions()
    }

    private fun getTransactions() = transactions.values.sortedByDescending { it.modifyTime }

    private fun deleteTransaction(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transactions.remove(it.id) }
        return getTransactions()
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

    override fun onSearchPressed() {
        view?.showSearchTransaction()
    }

    override fun onExportPressed() {
        val file = repository.getTransactionsFile()
        val stringBuilder = StringBuilder()
        stringBuilder.append(TransactionFields.HEAD_LINE)

        getTransactions().forEach {
            stringBuilder.append(TransactionFields.formatTransaction(it))
        }
        file.writeBytes(stringBuilder.toString().toByteArray())

        view?.showShareFileChooser(file)
    }

    override fun onProofVerificationPressed() {
        view?.showProofVerification()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            when (data.action) {
                ChangeAction.REMOVED -> deleteTransaction(data.tx)
                else -> updateTransactions(data.tx)
            }

            val transactions = deleteTransaction(repository.getAllTransactionInTrash())

            view?.configTransactions(transactions)
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    view?.configTransactions(deleteTransaction(it.data.transactions))
                }

                TrashManager.ActionType.Restored -> {
                    view?.configTransactions(updateTransactions(it.data.transactions))
                }

                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription, trashSubscription)

}