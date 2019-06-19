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
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Utxo

/**
 * Created by vain onnellinen on 4/5/19.
 */
class UtxosPagerAdapter (val context: Context, onUtxoClickListener: UtxosAdapter.OnItemClickListener) : androidx.viewpager.widget.PagerAdapter() {
    private val activeAdapter = UtxosAdapter(context, onUtxoClickListener)
    private val allAdapter = UtxosAdapter(context, onUtxoClickListener)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_list, container, false) as ViewGroup
        layout.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = when (Tab.values()[position]) {
                Tab.ACTIVE -> activeAdapter
                Tab.ALL -> allAdapter
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
            Tab.ACTIVE -> activeAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
            Tab.ALL -> allAdapter.apply {
                setData(utxos)
                notifyDataSetChanged()
            }
        }
    }
}

enum class Tab(val value: Int) {
    ACTIVE(R.string.active), ALL(R.string.all);

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

