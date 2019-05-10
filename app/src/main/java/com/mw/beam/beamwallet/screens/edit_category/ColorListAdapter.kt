package com.mw.beam.beamwallet.screens.edit_category

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mw.beam.beamwallet.core.helpers.CategoryColor
import com.mw.beam.beamwallet.core.views.ColorSelector

class ColorListAdapter: RecyclerView.Adapter<ColorListAdapter.ViewHolder>() {
    private val data = ArrayList<CategoryColor>()
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(ColorSelector(parent.context))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index = getItemId(position).toInt()

        holder.colorSelector.setOnClickListener {
            selectedIndex = index
            notifyDataSetChanged()
        }

        holder.colorSelector.isSelectedColor = selectedIndex == index
        holder.colorSelector.colorResId = data[index].getAndroidColorId()
    }

    fun setData(colors: List<CategoryColor>) {
        data.clear()
        data.addAll(colors)
        notifyDataSetChanged()
    }

    fun getSelectedColor(): CategoryColor {
        return data[selectedIndex]
    }


    class ViewHolder(val colorSelector: ColorSelector): RecyclerView.ViewHolder(colorSelector)
}