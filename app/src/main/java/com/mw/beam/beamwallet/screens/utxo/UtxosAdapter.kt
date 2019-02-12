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
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_utxo.*

/**
 * Created by vain onnellinen on 12/18/18.
 */
class UtxosAdapter(private val context: Context, private var data: List<Utxo>, private val clickListener: OnItemClickListener) :
        RecyclerView.Adapter<UtxosAdapter.ViewHolder>() {
    private val sentBeamCurrency = ContextCompat.getDrawable(context, R.drawable.currency_beam_send)
    private val receivedBeamCurrency = ContextCompat.getDrawable(context, R.drawable.currency_beam_receive)
    private val unavailableBeamCurrency = ContextCompat.getDrawable(context, R.drawable.currency_beam)
    private val spentStatus = context.getString(R.string.utxo_status_spent)
    private val inProgressStatus = context.getString(R.string.utxo_status_in_progress)
    private val incomingStatus = context.getString(R.string.utxo_status_incoming)
    private val changeStatus = context.getString(R.string.utxo_status_change)
    private val outgoingStatus = context.getString(R.string.utxo_status_outgoing)
    private val maturingStatus = context.getString(R.string.utxo_status_maturing)
    private val unavailableStatus = context.getString(R.string.utxo_status_unavailable)
    private val resultRollbackStatus = context.getString(R.string.utxo_status_result_rollback)
    private val availableStatus = context.getString(R.string.utxo_status_available)

    private val receivedColor = ContextCompat.getColor(context, R.color.received_color)
    private val sentColor = ContextCompat.getColor(context, R.color.sent_color)
    private val unavailableColor = ContextCompat.getColor(context, R.color.common_text_color)
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_not_multiply_color)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_utxo, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utxo = data[position]

        holder.apply {
            when (utxo.status) {
                UtxoStatus.Available, UtxoStatus.Maturing, UtxoStatus.Incoming -> {
                    amount.setTextColor(receivedColor)
                    status.setTextColor(receivedColor)
                    currency.setImageDrawable(receivedBeamCurrency)
                }
                UtxoStatus.Outgoing, UtxoStatus.Change, UtxoStatus.Spent -> {
                    amount.setTextColor(sentColor)
                    status.setTextColor(sentColor)
                    currency.setImageDrawable(sentBeamCurrency)
                }
                UtxoStatus.Unavailable -> {
                    amount.setTextColor(unavailableColor)
                    status.setTextColor(unavailableColor)
                    currency.setImageDrawable(unavailableBeamCurrency)
                }
            }

            status.text = when (utxo.status) {
                UtxoStatus.Incoming, UtxoStatus.Change, UtxoStatus.Outgoing -> inProgressStatus
                UtxoStatus.Maturing -> maturingStatus
                UtxoStatus.Spent -> spentStatus
                UtxoStatus.Available -> availableStatus
                UtxoStatus.Unavailable -> unavailableStatus
            }

            detailedStatus.visibility = View.VISIBLE
            detailedStatus.text = when (utxo.status) {
                UtxoStatus.Incoming -> incomingStatus
                UtxoStatus.Change -> changeStatus
                UtxoStatus.Outgoing -> outgoingStatus
                UtxoStatus.Unavailable -> resultRollbackStatus
                UtxoStatus.Maturing, UtxoStatus.Spent, UtxoStatus.Available -> {
                    detailedStatus.visibility = View.GONE
                    null //TODO add correct description for maturing
                }
            }

            itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor)
            amount.text = utxo.amount.convertToBeamString()
            id.text = utxo.stringId
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

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}
