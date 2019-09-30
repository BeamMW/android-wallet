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

class TransactionsPresenter(view: TransactionsContract.View?, repository: TransactionsContract.Repository)
    : BasePresenter<TransactionsContract.View, TransactionsContract.Repository>(view, repository), TransactionsContract.Presenter {

    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    private fun getTransactions() = AppManager.instance.getTransactions().sortedByDescending { it.modifyTime }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

    override fun onSearchPressed() {
        view?.showSearchTransaction()
    }

    override fun onExportSave() {
        val stringBuilder = StringBuilder()
        stringBuilder.append(TransactionFields.HEAD_LINE)

        getTransactions().forEach {
            stringBuilder.append(TransactionFields.formatTransaction(it))
        }

        view?.exportSave(stringBuilder.toString())
    }

    override fun onExportShare() {
        val file = repository.getTransactionsFile()
        val stringBuilder = StringBuilder()
        stringBuilder.append(TransactionFields.HEAD_LINE)

        getTransactions().forEach {
            stringBuilder.append(TransactionFields.formatTransaction(it))
        }
        file.writeBytes(stringBuilder.toString().toByteArray())

        view?.exportShare(file)
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
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription)

}