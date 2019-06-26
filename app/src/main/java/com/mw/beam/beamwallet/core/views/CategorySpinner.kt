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

package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper
import kotlinx.android.synthetic.main.category_spinner.view.*

class CategorySpinner : LinearLayout {
    private lateinit var categorySpinner: NDSpinner
    private lateinit var emptyMessage: TextView
    private var onChangeCategoryListener: OnChangeCategoryListener? = null
    private var selectFromCode = false
    private val allCategory: List<Category> by lazy {
        ArrayList(CategoryHelper.getAllCategory()).apply {
            add(0, CategoryHelper.noneCategory)
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        orientation = VERTICAL

        inflate(context, R.layout.category_spinner, this)
        categorySpinner = spinner
        emptyMessage = emptyCategoryListMessage


        categorySpinner.adapter = CategoryAdapter(context, allCategory)
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!selectFromCode) {
                    if (allCategory.size > 1) {
                        val category = allCategory[position]
                        onChangeCategoryListener?.onSelect(if (category == CategoryHelper.noneCategory) null else category)
                    } else {
                        onChangeCategoryListener?.onAddNewCategoryPressed()
                    }
                }
                selectFromCode = false
            }
        }

        emptyMessage.visibility = if (allCategory.size <= 1) View.VISIBLE else View.GONE
    }

    fun setOnChangeCategoryListener(onChangeCategoryListener: OnChangeCategoryListener?) {
        this.onChangeCategoryListener = onChangeCategoryListener
    }

    fun selectCategory(category: Category?) {
        selectFromCode = true
        if (category == null) {
            categorySpinner.setSelectionBaseMethod(0)
        } else {
            categorySpinner.setSelectionBaseMethod(allCategory.indexOfFirst { it.id == category.id })
        }
    }


    interface OnChangeCategoryListener {
        fun onSelect(category: Category?)
        fun onAddNewCategoryPressed()
    }

    private class CategoryAdapter(context: Context, private val categories: List<Category>) : ArrayAdapter<Category>(context, R.layout.item_category, categories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createCategoryView(convertView, position, false)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = createCategoryView(convertView, position, categories.size <= 1)

            val offset = context.resources.getDimensionPixelSize(R.dimen.settings_common_offset)
            view.setPadding(offset, offset / 2, offset, offset / 2)
            return view
        }

        private fun createCategoryView(convertView: View?, position: Int, addNewCategoryItem: Boolean): View {
            val view = convertView ?: CategoryItemView(context)
            val category = categories[position]
            val isNoneCategory = category.id == CategoryHelper.noneCategory.id

            (view as CategoryItemView).apply {
                colorResId = if (isNoneCategory) null else category.color.getAndroidColorId()

                text = when {
                    isNoneCategory && addNewCategoryItem -> context.getString(R.string.add_new_category)
                    isNoneCategory -> context.getString(R.string.none)
                    else -> category.name
                }

                enableCircleTitle = !addNewCategoryItem
            }
            return view
        }
    }
}