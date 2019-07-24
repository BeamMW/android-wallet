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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesAdapter(private val context: Context, private val clickListener: OnItemClickListener, private val categoryProvider: ((address: String) -> Category?)? = null, private val withExpireDate: Boolean = true) :
        androidx.recyclerview.widget.RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_not_multiply_color)
    private val noNameLabel = context.getString(R.string.no_name)

    private var data: List<WalletAddress> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(if (withExpireDate) R.layout.item_address else R.layout.item_send_address_suggestions, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = data[position]
        val addressCategory = categoryProvider?.invoke(address.walletID)

        holder.apply {
            itemView.findViewById<TextView>(R.id.label).text = if (address.label.isBlank()) noNameLabel else address.label
            itemView.findViewById<TextView>(R.id.addressId).text = address.walletID
            itemView.setBackgroundColor(if (position % 2 == 0)  notMultiplyColor else multiplyColor) //logically reversed because count starts from zero
            val dateTextView = itemView.findViewById<TextView>(R.id.date)
            dateTextView.visibility = if (address.isContact || !withExpireDate) View.GONE else View.VISIBLE
            if (!address.isContact && withExpireDate) {
//                dateTextView.text = "${if (address.isExpired) expiredDate else expiresDate}: ${if (address.duration == 0L) expiresNever else CalendarUtils.fromTimestamp(address.createTime + address.duration)}"
                val expireStateString = when {
                    address.isExpired -> {
                        val dateString = CalendarUtils.fromTimestamp(address.createTime + address.duration, SimpleDateFormat("d MMM yyyy", AppConfig.LOCALE))
                        "${context.getString(R.string.expired).toLowerCase()} $dateString"
                    }
                    address.duration == 0L -> context.getString(R.string.never_expires)
                    else -> {
//                        val calendar = CalendarUtils.calendarFromTimestamp(address.createTime + address.duration)
//                        val currentDate = Calendar.getInstance()
//                        val timeDiff = calendar.timeInMillis - currentDate.timeInMillis
//
//                        val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)


                        val timeLeftString = ""
                        context.getString(R.string.expires_in, timeLeftString).toLowerCase()
                    }
                }
            }

            val category = itemView.findViewById<TextView>(R.id.category)

            category.visibility = if (addressCategory == null) View.GONE else View.VISIBLE
            category.text = addressCategory?.name ?: ""

            if (addressCategory != null) {
                category.setTextColor(context.resources.getColor(addressCategory.color.getAndroidColorId(), context.theme))
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: List<WalletAddress>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: WalletAddress)
    }

    class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer
}
