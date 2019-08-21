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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxStatus
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter

class TransactionsPageAdapter(private val context: Context, onTxClickListener: (TxDescription) -> Unit): androidx.viewpager.widget.PagerAdapter()  {

    private var transactions: List<TxDescription> = listOf()
    private val allTxAdapter = TransactionsAdapter(context, listOf(), false, onTxClickListener)
    private val inProgressTxAdapter = TransactionsAdapter(context, listOf(),false, onTxClickListener)
    private val sentTxAdapter = TransactionsAdapter(context, listOf(), false, onTxClickListener)
    private val receivedTxAdapter = TransactionsAdapter(context, listOf(), false, onTxClickListener)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_list_placholder, container, false) as ViewGroup

        val recyclerView = layout.findViewById<com.mw.beam.beamwallet.core.views.RecyclerViewEmptySupport>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val emptyView = layout.findViewById<LinearLayout>(R.id.emptyLayout)

        val emptyLabel = emptyView.findViewById<TextView>(R.id.emptyLabel)
        emptyLabel.text = context.getString(R.string.wallet_empty_transactions_list_message)
        emptyLabel.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.drawable.ic_wallet_empty), null, null)

        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = when (TransactionTab.values()[position]) {
            TransactionTab.All -> allTxAdapter
            TransactionTab.InProgress -> inProgressTxAdapter
            TransactionTab.Sent -> sentTxAdapter
            TransactionTab.Received -> receivedTxAdapter
        }

        container.addView(layout)
        return layout
    }

    fun setData(data: List<TxDescription>) {
        transactions = data
        updateData()
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    private fun updateData() {
        allTxAdapter.data = transactions
        allTxAdapter.notifyDataSetChanged()

        sentTxAdapter.data = transactions.filter { it.sender.value && isCompletedTx(it)}
        sentTxAdapter.notifyDataSetChanged()

        receivedTxAdapter.data = transactions.filter { !it.sender.value && isCompletedTx(it) }
        receivedTxAdapter.notifyDataSetChanged()

        inProgressTxAdapter.data = transactions.filter { !isCompletedTx(it) }
        inProgressTxAdapter.notifyDataSetChanged()
    }


    override fun getPageTitle(position: Int): CharSequence? {
        val stringId = when (TransactionTab.values()[position]) {
            TransactionTab.All -> R.string.all
            TransactionTab.InProgress -> R.string.in_progress
            TransactionTab.Sent -> R.string.sent
            TransactionTab.Received -> R.string.received
        }

        return context.getString(stringId)
    }

    private fun isCompletedTx(tx: TxDescription): Boolean {
        return tx.status != TxStatus.Registered && tx.status != TxStatus.Pending && tx.status != TxStatus.InProgress
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getCount(): Int {
        return TransactionTab.values().size
    }
}

enum class TransactionTab {
    All, InProgress, Sent, Received
}