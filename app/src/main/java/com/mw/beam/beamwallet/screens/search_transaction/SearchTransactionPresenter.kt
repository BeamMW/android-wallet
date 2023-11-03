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
import com.mw.beam.beamwallet.core.AppManager
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
        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            onSearchTextChanged(state.searchText)
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
            state.getAllTransactions(view?.getAssetId() ?: -1).filter {
                it.id.toLowerCase().startsWith(state.searchText) ||
                        it.kernelId.toLowerCase().startsWith(state.searchText) ||
                        it.senderAddress.toLowerCase().startsWith(state.searchText) ||
                        it.token.toLowerCase().startsWith(state.searchText) ||
                        it.receiverAddress.toLowerCase().startsWith(state.searchText) ||
                        findWalletAddress(it, state.searchText) ||
                        it.message.toLowerCase().contains(state.searchText)
            }
        } else {
            listOf()
        }.sortedByDescending { it.createTime }

        view?.configTransactions(transactions, repository.isPrivacyModeEnabled(), state.searchText)
    }

    private fun findWalletAddress(txDescription: TxDescription, searchText: String): Boolean {
        return state.getAddresses().filter { it.id == txDescription.myId || it.id == txDescription.peerId }
                .any { it.label.toLowerCase().contains(searchText) }
    }

    override fun onTransactionPressed(txDescription: TxDescription) {
        view?.showTransactionDetails(txDescription.id)
    }

}