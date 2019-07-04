package com.mw.beam.beamwallet.screens.wallet

import androidx.recyclerview.widget.DiffUtil
import com.mw.beam.beamwallet.core.entities.TxDescription

class TransactionDiffUtilCallback(private val oldItems: List<TxDescription>, private val newItems: List<TxDescription>): DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem.source == newItem.source
    }
}