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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_utxo.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.helpers.*

import kotlinx.android.synthetic.main.item_utxo.*

/**
 *  12/18/18.
 */
class UtxosAdapter(private val context: Context, private val clickListener: OnItemClickListener) :
        androidx.recyclerview.widget.RecyclerView.Adapter<UtxosAdapter.ViewHolder>() {
    private val spentStatus = context.getString(R.string.spent)
    private val incomingStatus = context.getString(R.string.incoming)
    private val changeStatus = context.getString(R.string.change_utxo_type)
    private val outgoingStatus = context.getString(R.string.outgoing)
    private val maturingStatus = context.getString(R.string.maturing)
    private val unavailableStatus = context.getString(R.string.unavailable)
    private val availableStatus = context.getString(R.string.available)
    private val tillBlockHeight = context.getString(R.string.till_block_height)

    private val receivedColor = ContextCompat.getColor(context, R.color.received_color)
    private val sentColor = ContextCompat.getColor(context, R.color.sent_color)
    private val commonStatusColor = ContextCompat.getColor(context, R.color.common_text_color)
    private val accentColor = ContextCompat.getColor(context, R.color.accent)
    private val unavailableColor = if(App.isDarkMode) {
        (ContextCompat.getColor(context, R.color.common_text_dark_color_dark))
    }
    else{
        (ContextCompat.getColor(context, R.color.common_text_dark_color))
    }
    
    private var data: List<Utxo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_utxo, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)

        val utxo = data[position]

        holder.apply {
            statusLabel.setTextColor(when (utxo.status) {
                UtxoStatus.Available -> accentColor
                UtxoStatus.Incoming -> receivedColor
                UtxoStatus.Unavailable -> unavailableColor
                UtxoStatus.Outgoing, UtxoStatus.Spent -> sentColor
                UtxoStatus.Change, UtxoStatus.Maturing  -> commonStatusColor
            })

            statusLabel.text = when (utxo.status) {
                UtxoStatus.Incoming -> incomingStatus
                UtxoStatus.Change -> changeStatus
                UtxoStatus.Outgoing -> outgoingStatus
                UtxoStatus.Maturing -> maturingStatus
                UtxoStatus.Spent -> spentStatus
                UtxoStatus.Available -> availableStatus
                UtxoStatus.Unavailable -> unavailableStatus
            }.toLowerCase() + " "


            if (App.isDarkMode) {
                itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_not_multiply_color_dark else R.color.colorClear)
            }
            else{
                itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_multiply_color else R.color.colorClear)
            }

            val asset = AssetManager.instance.getAsset(utxo.assetId)
            assetIcon.setImageResource(asset?.image ?: R.drawable.ic_asset_0)
            amountLabel.text = utxo.amount.convertToAssetString(asset?.unitName ?: "")

            typeLabel.text = when (utxo.keyType) {
                UtxoKeyType.Commission -> context.getString(R.string.commission)
                UtxoKeyType.Coinbase -> context.getString(R.string.coinbase)
                UtxoKeyType.Regular -> context.getString(R.string.regular)
                UtxoKeyType.Change -> context.getString(R.string.change_utxo_type)
                UtxoKeyType.Kernel -> context.getString(R.string.kernel)
                UtxoKeyType.Kernel2 -> context.getString(R.string.kernel2)
                UtxoKeyType.Identity -> context.getString(R.string.identity)
                UtxoKeyType.ChildKey -> context.getString(R.string.childKey)
                UtxoKeyType.Bbs -> context.getString(R.string.bbs)
                UtxoKeyType.Decoy -> context.getString(R.string.decoy)
                UtxoKeyType.Treasury -> context.getString(R.string.treasure)
                UtxoKeyType.Shielded -> context.getString(R.string.shielded)
            }

            if (utxo.status == UtxoStatus.Maturing) {
                dateLabel.visibility = View.VISIBLE
                dateLabel.text = tillBlockHeight + " " + utxo.maturity
            }
            else if (utxo.transactionComment != null && utxo.transactionDate != null)
            {
                dateLabel.visibility = View.VISIBLE
                dateLabel.text = CalendarUtils.fromTimestampShort(utxo.transactionDate!!)
            }
            else if (utxo.transactionDate != null)
            {
                dateLabel.visibility = View.VISIBLE
                dateLabel.text = CalendarUtils.fromTimestampShort(utxo.transactionDate!!)
            }
            else{
                dateLabel.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: List<Utxo>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: Utxo)
    }

    class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer
}
