package com.mw.beam.beamwallet.screens.change_address

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.item_address_with_transaction.view.*

class SearchAddressesAdapter(private val context: Context, private val listener: OnSearchAddressClickListener): RecyclerView.Adapter<SearchAddressesAdapter.ViewHolder>() {
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_not_multiply_color)

    private var data: List<SearchItem> = listOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_address_with_transaction, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.apply {
            itemView.setBackgroundColor(if (position % 2 == 0)  notMultiplyColor else multiplyColor)
            itemView.setOnClickListener {
                listener.onClick(item.walletAddress)
            }

            itemView.label.text = item.walletAddress.label
            itemView.addressId.text = item.walletAddress.walletID

            val addressCategory = item.category;
            if (addressCategory != null) {
                itemView.category.visibility = View.VISIBLE
                itemView.category.text = addressCategory.name
                itemView.category.setTextColor(context.resources.getColor(addressCategory.color.getAndroidColorId(), context.theme))
            } else {
                itemView.category.visibility = View.GONE
            }

            val transaction = item.lastTransaction
            if (transaction != null) {
                itemView.txComment.visibility = if (transaction.message.isBlank()) View.GONE else View.VISIBLE
                itemView.commentIcon.visibility = if (transaction.message.isBlank()) View.GONE else View.VISIBLE

                itemView.txDate.visibility = View.VISIBLE
                itemView.txDate.text = CalendarUtils.fromTimestampShort(transaction.modifyTime)

                itemView.txComment.text = transaction.message
            } else {
                itemView.txDate.visibility = View.GONE
                itemView.txComment.visibility = View.GONE
                itemView.commentIcon.visibility = View.GONE
            }
        }
    }

    fun setData(data: List<SearchItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    interface OnSearchAddressClickListener {
        fun onClick(walletAddress: WalletAddress)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

}