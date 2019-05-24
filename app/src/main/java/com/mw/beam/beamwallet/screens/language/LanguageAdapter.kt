package com.mw.beam.beamwallet.screens.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R

class LanguageAdapter(private val languages: List<String>, private val onSelected: (Int) -> Unit): RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private var selectedIndex = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun getItemCount(): Int = languages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.selected = selectedIndex == position
        holder.text = languages[position]
        holder.itemView.setOnClickListener { onSelected(position) }
    }

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var selected: Boolean = false
            set(value) {
                field = value
                val endDrawable = if (field) itemView.context.getDrawable(R.drawable.ic_list_ckecked) else null
                itemView.findViewById<TextView>(R.id.language).setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, endDrawable, null)
            }

        var text: String = ""
            set(value) {
                field = value
                itemView.findViewById<TextView>(R.id.language).text = field
            }
    }
}