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

package com.mw.beam.beamwallet.screens.search_transaction

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import io.reactivex.disposables.Disposable

class SearchTransactionPresenter(view: SearchTransactionContract.View?, repository: SearchTransactionContract.Repository, private val state: SearchTransactionState)
    : BasePresenter<SearchTransactionContract.View, SearchTransactionContract.Repository>(view, repository), SearchTransactionContract.Presenter {
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onStart() {
        super.onStart()
        onSearchTextChanged(state.searchText)
    }

    override fun initSubscriptions() {
        txStatusSubscription = repository.getTxStatus().subscribe {
            state.updateTransactions(it.tx)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(txStatusSubscription)

    override fun onClearPressed() {
        view?.clearSearchText()
    }

    override fun onSearchTextChanged(text: String) {
        state.searchText = text.trim().toLowerCase()

        view?.setClearButtonVisible(state.searchText.isNotBlank())

        val transactions = if (state.searchText.isNotBlank()) {
            state.getAllTransactions().filter {
                it.id.toLowerCase().startsWith(state.searchText) ||
                        it.peerId.toLowerCase().startsWith(state.searchText) ||
                        it.myId.toLowerCase().startsWith(state.searchText) ||
                        it.message.toLowerCase().contains(state.searchText)
            }
        } else {
            listOf()
        }.sortedByDescending { it.createTime }

        view?.configTransactions(transactions, repository.isPrivacyModeEnabled(), state.searchText)
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

}