package com.mw.beam.beamwallet.core.views

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper
import kotlinx.android.synthetic.main.item_selectable_tag.view.*

class TagAdapter(private val onSelectedChangeListener: (List<Tag>) -> Unit): RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    private val allTags: List<Tag> = TagHelper.getAllTags()
    private var selectedTags: HashMap<String, Tag> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_selectable_tag, parent, false))
    }

    override fun getItemCount(): Int = allTags.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = allTags[position]

        holder.apply {
            circleColorId = tag.color.getAndroidColorId()
            name = tag.name
            isSelected = selectedTags.any { it.key == tag.id }

            setOnChangeSelectedListener {
                if (it) {
                    selectedTags[tag.id] = tag
                } else {
                    selectedTags.remove(tag.id)
                }

                onSelectedChangeListener.invoke(selectedTags.values.toList())
            }
        }
    }

    fun setSelectedTags(tags: List<Tag>) {
        selectedTags = HashMap(tags.associateBy { it.id })
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var circleColorId: Int = 0
            set(value) {
                field = value
                itemView.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(App.self, value))
            }

        var name: String = ""
            set(value) {
                field = value
                itemView.categoryName.text = field
            }

        var isSelected: Boolean = false
            set(value) {
                field = value
                itemView.tagCheckbox.isChecked = field
            }

        fun setOnChangeSelectedListener(function: (Boolean) -> Unit) {
            val checkbox = itemView.tagCheckbox
            itemView.setOnClickListener { checkbox.isChecked = !checkbox.isChecked }
            checkbox.setOnCheckedChangeListener { _, isChecked ->  function.invoke(isChecked)}
        }

    }
}