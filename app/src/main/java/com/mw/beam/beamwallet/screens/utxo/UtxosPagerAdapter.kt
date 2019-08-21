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

package com.mw.beam.beamwallet.screens.utxo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.screens.addresses.AddressPagerType
import androidx.core.content.ContextCompat
import kotlin.math.exp

/**
 * Created by vain onnellinen on 4/5/19.
 */
class UtxosPagerAdapter (val context: Context, onUtxoClickListener: UtxosAdapter.OnItemClickListener) : androidx.viewpager.widget.PagerAdapter() {

    private val availableAdapter = UtxosAdapter(context, onUtxoClickListener)
    private val progressAdapter = UtxosAdapter(context, onUtxoClickListener)
    private val spentAdapter = UtxosAdapter(context, onUtxoClickListener)
    private val unavailableAdapter = UtxosAdapter(context, onUtxoClickListener)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_list_placholder, container, false) as ViewGroup

        val recyclerView = layout.findViewById<com.mw.beam.beamwallet.core.views.RecyclerViewEmptySupport>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val emptyView = layout.findViewById<LinearLayout>(R.id.emptyLayout)

        val emptyLabel = emptyView.findViewById<TextView>(R.id.emptyLabel)
        emptyLabel.text = context.getString(R.string.empty_utxo_list)
        emptyLabel.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_utxo_empty_state, 0, 0)

        recyclerView.setEmptyView(emptyView)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = when (Tab.values()[position]) {
                Tab.AVAILABLE -> availableAdapter
                Tab.PROGRESS -> progressAdapter
                Tab.SPENT -> spentAdapter
                Tab.UNAVAILABLE -> unavailableAdapter
            }
        }

        container.addView(layout)

        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getCount(): Int = Tab.values().count()

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(Tab.values()[position].value)
    }

    fun setData(tab: Tab, utxos: List<Utxo>) {
        when (tab) {
            Tab.AVAILABLE -> availableAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
            Tab.PROGRESS -> progressAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
            Tab.SPENT -> spentAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
            Tab.UNAVAILABLE -> unavailableAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
        }
    }
}

enum class Tab(val value: Int) {
    AVAILABLE(R.string.available), PROGRESS(R.string.in_progress),
    SPENT(R.string.spent), UNAVAILABLE(R.string.unavailable);

    companion object {
        private val map: HashMap<Int, Tab> = HashMap()

        init {
            values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): Tab {
            return map[type] ?: throw IllegalArgumentException("Unknown Tab")
        }
    }
}

