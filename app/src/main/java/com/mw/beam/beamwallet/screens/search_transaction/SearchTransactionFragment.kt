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

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter
import kotlinx.android.synthetic.main.fragment_search_transaction.*

class SearchTransactionFragment : BaseFragment<SearchTransactionPresenter>(), SearchTransactionContract.View {
    private lateinit var adapter: TransactionsAdapter
    private val searchTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onSearchTextChanged(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_search_transaction

    override fun getToolbarTitle(): String? = ""

    override fun init() {
        adapter = TransactionsAdapter(context!!, mutableListOf(), false) {
            presenter?.onTransactionPressed(it)
        }
        adapter.invertItemColors = true

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setEmptyView(emptyLabel)
    }

    override fun addListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnClear.setOnClickListener {
            presenter?.onClearPressed()
        }

        searchEditText.addTextChangedListener(searchTextWatcher)
    }

    override fun clearSearchText() {
        searchEditText.setText("")
    }

    override fun clearListeners() {
        btnBack.setOnClickListener(null)
        btnClear.setOnClickListener(null)

        searchEditText.removeTextChangedListener(searchTextWatcher)
    }

    override fun updateAddresses(addresses: List<WalletAddress>) {
        adapter.addresses = addresses
    }

    override fun configTransactions(transactions: List<TxDescription>, isEnablePrivacyMode: Boolean, searchText: String?) {
        adapter.setPrivacyMode(isEnablePrivacyMode)
        adapter.setSearchText(searchText)
        adapter.data = transactions
        adapter.notifyDataSetChanged()
    }

    override fun setClearButtonVisible(isVisible: Boolean) {
        btnClear.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun showTransactionDetails(txId: String) {
        findNavController().navigate(SearchTransactionFragmentDirections.actionSearchTransactionFragmentToTransactionDetailsFragment(txId))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SearchTransactionPresenter(this, SearchTransactionRepository(), SearchTransactionState())
    }
}