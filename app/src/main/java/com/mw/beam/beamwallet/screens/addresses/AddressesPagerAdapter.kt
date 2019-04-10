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

package com.mw.beam.beamwallet.screens.addresses

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.WalletAddress

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesPagerAdapter(val context: Context, onAddressClickListener: AddressesAdapter.OnItemClickListener) : PagerAdapter() {
    private val activeAdapter = AddressesAdapter(context, onAddressClickListener)
    private val expiredAdapter = AddressesAdapter(context, onAddressClickListener)
    private val contactsAdapter = AddressesAdapter(context, onAddressClickListener)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_list, container, false) as ViewGroup
        layout.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = when (Tab.values()[position]) {
                Tab.ACTIVE -> activeAdapter
                Tab.EXPIRED -> expiredAdapter
                Tab.CONTACTS -> contactsAdapter
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

    fun setData(tab: Tab, addresses: List<WalletAddress>) {
        when (tab) {
            Tab.ACTIVE -> activeAdapter.apply {
                setData(addresses)
                notifyDataSetChanged()
            }
            Tab.EXPIRED -> expiredAdapter.apply {
                setData(addresses)
                notifyDataSetChanged()
            }
            Tab.CONTACTS -> contactsAdapter.apply {
                setData(addresses)
                notifyDataSetChanged()
            }
        }
    }
}

enum class Tab(val value: Int) {
    ACTIVE(R.string.addresses_tab_active), EXPIRED(R.string.addresses_tab_expired), CONTACTS(R.string.addresses_tab_contacts);

    companion object {
        private val map: HashMap<Int, Tab> = HashMap()

        init {
            Tab.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): Tab {
            return map[type] ?: throw IllegalArgumentException("Unknown Tab")
        }
    }
}
