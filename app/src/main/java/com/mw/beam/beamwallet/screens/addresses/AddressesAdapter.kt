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
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.BMAddressType
import com.mw.beam.beamwallet.core.entities.WalletAddress
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_address.*
import java.util.*
import com.mw.beam.beamwallet.core.helpers.selector

/**
 *  2/28/19.
 */
class AddressesAdapter(private val context: Context,
                       private val clickListener: OnItemClickListener,
                       private val longListener: OnLongClickListener?,
                       private val generatedAddress: WalletAddress? = null) : androidx.recyclerview.widget.RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {

    private val noNameLabel = context.getString(R.string.no_name)
    private val autoGeneratedLabel = context.getString(R.string.auto_generated).toLowerCase()

    private val labelTypeface = ResourcesCompat.getFont(context, R.font.roboto_regular)

    private var data: List<WalletAddress> = listOf()

    var selectedAddresses = mutableListOf<String>()
    var mode = AddressesFragment.Mode.NONE
    var displayAddressType = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_address, parent, false)).apply {
        this.containerView.setOnClickListener {

            clickListener.onItemClick(data[adapterPosition])

            if (mode == AddressesFragment.Mode.EDIT) {
                if (selectedAddresses.contains(data[adapterPosition].id)) {
                    selectedAddresses.remove(data[adapterPosition].id)
                } else {
                    selectedAddresses.add(data[adapterPosition].id)
                }

                checkBox.isChecked = selectedAddresses.contains(data[adapterPosition].id)
            }
        }

        if (longListener != null) {
            this.containerView.setOnLongClickListener {
                longListener?.onLongClick(data[adapterPosition])
                return@setOnLongClickListener true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = data[position]

        holder.apply {
            val isGeneratedAddress = address.id == generatedAddress?.id

            val labelView = itemView.findViewById<TextView>(R.id.label)
            labelView.text = when {
                isGeneratedAddress -> "$noNameLabel ($autoGeneratedLabel)"
                address.label.isNotBlank() -> address.label
                else -> noNameLabel
            }

            labelView.setTypeface(labelTypeface, Typeface.NORMAL)

            itemView.findViewById<TextView>(R.id.addressId).text = address.displayAddress ?: address.address

            if (App.isDarkMode) {
                itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_not_multiply_color_dark else R.color.colorClear)
            }
            else{
                itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_multiply_color else R.color.colorClear)
            }

            checkBox.setOnClickListener {

                if (selectedAddresses.contains(data[adapterPosition].id)) {
                    selectedAddresses.remove(data[adapterPosition].id)
                } else {
                    selectedAddresses.add(data[adapterPosition].id)
                }

                clickListener.onItemClick(address)
            }

            if (mode == AddressesFragment.Mode.NONE) {
                checkBox.isChecked = false
                checkBox.visibility = View.GONE
            } else {
                checkBox.isChecked = selectedAddresses.contains(address.id)
                checkBox.visibility = View.VISIBLE
            }

            val addressType = itemView.findViewById<TextView>(R.id.addressTypeLabel)
            if(displayAddressType) {
                addressType.visibility = View.VISIBLE
                val params = AppManager.instance.wallet?.getTransactionParameters(address.id, false)
                if(params!=null) {
                    when (params.getAddressType()) {
                        BMAddressType.BMAddressTypeMaxPrivacy -> {
                            addressType.text = context.getString(R.string.max_privacy)
                        }
                        BMAddressType.BMAddressTypeOfflinePublic -> {
                            addressType.text = context.getString(R.string.public_offline)
                        }
                        else -> {
                            addressType.text = context.getString(R.string.regular)
                        }
                    }
                }
                else {
                    addressType.text = context.getString(R.string.regular)
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size
    override fun getItemId(position: Int): Long = position.toLong()

    fun item(index: Int): WalletAddress {
        return data[index]
    }

    fun setData(data: List<WalletAddress>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: WalletAddress)
    }

    interface OnLongClickListener {
        fun onLongClick(item: WalletAddress)
    }

    class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer
}
