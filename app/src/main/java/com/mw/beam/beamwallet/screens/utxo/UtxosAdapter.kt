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
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_utxo.*
import kotlinx.android.synthetic.main.item_utxo.*
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.mw.beam.beamwallet.core.helpers.ScreenHelper



/**
 * Created by vain onnellinen on 12/18/18.
 */
class UtxosAdapter(private val context: Context, private val clickListener: OnItemClickListener) :
        androidx.recyclerview.widget.RecyclerView.Adapter<UtxosAdapter.ViewHolder>() {
    private val spentStatus = context.getString(R.string.spent)
    private val inProgressStatus = context.getString(R.string.in_progress)
    private val incomingStatus = context.getString(R.string.incoming)
    private val changeStatus = context.getString(R.string.change)
    private val outgoingStatus = context.getString(R.string.outgoing)
    private val maturingStatus = context.getString(R.string.maturing)
    private val unavailableStatus = context.getString(R.string.unavailable)
    private val resultRollbackStatus = context.getString(R.string.utxo_status_result_rollback)
    private val availableStatus = context.getString(R.string.available)

    private val receivedColor = ContextCompat.getColor(context, R.color.received_color)
    private val sentColor = ContextCompat.getColor(context, R.color.sent_color)
    private val commonStatusColor = ContextCompat.getColor(context, R.color.common_text_color)
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_not_multiply_color)
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.colorClear)

    private var data: List<Utxo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_utxo, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utxo = data[position]

        holder.apply {
            status.setTextColor(when (utxo.status) {
                UtxoStatus.Incoming -> receivedColor
                UtxoStatus.Outgoing, UtxoStatus.Spent -> sentColor
                UtxoStatus.Change, UtxoStatus.Maturing, UtxoStatus.Available, UtxoStatus.Unavailable -> commonStatusColor
            })

            status.text = when (utxo.status) {
                UtxoStatus.Incoming -> incomingStatus
                UtxoStatus.Change -> changeStatus
                UtxoStatus.Outgoing -> outgoingStatus
                UtxoStatus.Maturing -> maturingStatus
                UtxoStatus.Spent -> spentStatus
                UtxoStatus.Available -> availableStatus
                UtxoStatus.Unavailable -> unavailableStatus
            }.toLowerCase() + " "


            itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor)
            amount.text = utxo.amount.convertToBeamString()

            if (utxo.transactionComment != null && utxo.transactionDate != null)
            {
                commentLayout.visibility = View.VISIBLE
                dateLabel.visibility = View.VISIBLE

                dateLabel.text = CalendarUtils.fromTimestampShort(utxo.transactionDate!!)

                status.setPadding(0,0,0,0)

                if (with(utxo.transactionComment!!) { isEmpty() })
                {
                    amount.setPadding(0,25,0,25)

                    commentLabel.visibility = View.GONE
                    commentIcon.visibility = View.GONE
                }
                else{
                    amount.setPadding(0,0,0,0)

                    commentLabel.text = "“" + utxo.transactionComment + "“"
                    commentLabel.visibility = View.VISIBLE
                    commentIcon.visibility = View.VISIBLE
                }
            }
            else if (utxo.transactionDate != null)
            {
                commentLayout.visibility = View.VISIBLE
                dateLabel.visibility = View.VISIBLE

                dateLabel.text = CalendarUtils.fromTimestampShort(utxo.transactionDate!!)

                status.setPadding(0,0,0,0)

                amount.setPadding(0,25,0,25)

                commentLabel.visibility = View.GONE
                commentIcon.visibility = View.GONE
            }
            else{
                amount.setPadding(0,20,0,20)
                status.setPadding(0,25,0,25)

                commentLayout.visibility = View.GONE
                dateLabel.visibility = View.GONE
                commentLabel.visibility = View.GONE
                commentIcon.visibility = View.GONE
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
