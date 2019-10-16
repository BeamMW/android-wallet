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

package com.mw.beam.beamwallet.screens.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter

/**
 *  2/28/19.
 */
class CategoriesPagerAdapter(val context: Context, var categoryID:String, onAddressClickListener: AddressesAdapter.OnItemClickListener) : androidx.viewpager.widget.PagerAdapter() {
    private var touchListener: View.OnTouchListener? = null

    private val addressesAdapter = AddressesAdapter(context, onAddressClickListener,null, {
        return@AddressesAdapter onSearchTagsForAddress(it) ?: listOf()
    })


    private val contactsAdapter = AddressesAdapter(context, onAddressClickListener,null, {
        return@AddressesAdapter onSearchTagsForAddress(it) ?: listOf()
    })

    private var addressesLayoutManager: LinearLayoutManager? = null
    private var contactsLayoutManager: LinearLayoutManager? = null

    private fun onSearchTagsForAddress(address: String): List<Tag> {
        return TagHelper.getTagsForAddress(address)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val layout = LayoutInflater.from(context).inflate(R.layout.item_list_placholder, container, false) as ViewGroup

        val recyclerView = layout.findViewById<com.mw.beam.beamwallet.core.views.RecyclerViewEmptySupport>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val emptyView = layout.findViewById<LinearLayout>(R.id.emptyLayout)

        val emptyLabel = emptyView.findViewById<TextView>(R.id.emptyLabel)

        when (position) {
            Tab.CONTACTS.value -> emptyLabel.text = context.getString(R.string.empty_contacts_list)
            Tab.ADDRESSES.value -> emptyLabel.text = context.getString(R.string.empty_address_list)
        }

        recyclerView.setEmptyView(emptyView)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.apply {
            this.layoutManager = layoutManager

            adapter = when (Tab.values()[position]) {
                Tab.ADDRESSES -> addressesAdapter
                Tab.CONTACTS -> contactsAdapter
            }

            setOnTouchListener { v, event -> touchListener?.onTouch(v, event) ?: false }
        }

        when (Tab.values()[position]) {
            Tab.ADDRESSES -> addressesLayoutManager = layoutManager
            Tab.CONTACTS -> contactsLayoutManager = layoutManager
        }

        container.addView(layout)

        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        when (Tab.values()[position]) {
            Tab.ADDRESSES -> addressesLayoutManager = null
            Tab.CONTACTS -> contactsLayoutManager = null
        }

        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getCount(): Int = Tab.values().count()

    override fun getPageTitle(position: Int): CharSequence? {
        val stringId = when (Tab.values()[position]) {
            Tab.ADDRESSES -> R.string.addresses
            Tab.CONTACTS -> R.string.contacts
        }
        return context.getString(stringId)
    }


    fun setData(tab: Tab, addresses: List<WalletAddress>) {

        when (tab) {
            Tab.ADDRESSES -> addressesAdapter.apply {
                setData(addresses.sortedByDescending { it.createTime })
                notifyDataSetChanged()
            }
            Tab.CONTACTS -> contactsAdapter.apply {
                setData(addresses.sortedByDescending { it.createTime })
                notifyDataSetChanged()
            }
        }
    }
}

enum class Tab(val value: Int) {
    ADDRESSES(0),
    CONTACTS(1)
}
