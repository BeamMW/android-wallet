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

package com.mw.beam.beamwallet.screens.choose_currency

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

class ChooseCurrencyAdapter(private val assets: List<Asset>, private val onSelected: (Int) -> Unit): RecyclerView.Adapter<ChooseCurrencyAdapter.ViewHolder>() {
    var selectedAsset = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false))
    }

    override fun getItemCount(): Int = assets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val asset = assets[position]

        holder.selected = asset.assetId == selectedAsset
        holder.name = asset.unitName
        holder.itemView.setOnClickListener { onSelected(asset.assetId) }
    }

    fun currency(id: Int) {
        selectedAsset = id
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var selected: Boolean = false
            set(value) {
                field = value
                val visibility = if (field) View.VISIBLE else View.GONE
                itemView.findViewById<ImageView>(R.id.selectedImage).visibility = visibility
            }

        var name: String = ""
            set(value) {
                field = value
                itemView.findViewById<TextView>(R.id.name).text = field
            }
    }
}