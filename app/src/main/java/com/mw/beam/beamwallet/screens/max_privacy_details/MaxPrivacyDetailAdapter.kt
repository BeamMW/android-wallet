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

package com.mw.beam.beamwallet.screens.max_privacy_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.selector

class MaxPrivacyDetailAdapter(private var utxos: List<Utxo>, private val onSelected: (Utxo) -> Unit): RecyclerView.Adapter<MaxPrivacyDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_utxo_max_privacy, parent, false))
    }

    override fun getItemCount(): Int = utxos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utxo = utxos[position]
        holder.itemView.findViewById<TextView>(R.id.amountLabel).text = utxo.amount.convertToBeamString() + " BEAM"
        holder.itemView.findViewById<TextView>(R.id.timeLabel).text = utxo.timeLeft

        if (App.isDarkMode) {
            holder.itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_not_multiply_color_dark else R.color.colorClear)
        }
        else{
            holder.itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_multiply_color else R.color.colorClear)
        }
    }

    fun reload(values: List<Utxo>) {
        utxos = values
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }
}