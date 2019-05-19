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

package com.mw.beam.beamwallet.screens.edit_category

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mw.beam.beamwallet.core.helpers.CategoryColor
import com.mw.beam.beamwallet.core.views.ColorSelector

class ColorListAdapter(private val onSelectColor: ((CategoryColor) -> Unit)? = null): RecyclerView.Adapter<ColorListAdapter.ViewHolder>() {
    private val data = ArrayList<CategoryColor>()
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(ColorSelector(parent.context))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorSelector.setOnClickListener {
            selectedIndex = position
            onSelectColor?.invoke(data[selectedIndex])
            notifyDataSetChanged()
        }

        holder.colorSelector.isSelectedColor = selectedIndex == position
        holder.colorSelector.colorResId = data[position].getAndroidColorId()
    }

    fun setData(colors: List<CategoryColor>) {
        data.clear()
        data.addAll(colors)
        notifyDataSetChanged()
    }

    fun setSelectedColor(color: CategoryColor) {
        selectedIndex = data.indexOf(color)
        if (selectedIndex < 0){
            selectedIndex = 0
        }
        notifyDataSetChanged()
    }

    fun getSelectedColor(): CategoryColor {
        return data[selectedIndex]
    }


    class ViewHolder(val colorSelector: ColorSelector): RecyclerView.ViewHolder(colorSelector)
}