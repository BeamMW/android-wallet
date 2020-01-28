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
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App

import kotlinx.android.synthetic.main.item_category.view.*

class CategoryItemView: FrameLayout {
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    var colorResId: Int? = null
        set(value) {
            field = value

            if (value != null) {
                colorCircle.backgroundTintList = ColorStateList.valueOf(resources.getColor(value, App.self.theme))
            } else {
                colorCircle.backgroundTintList = null
            }
        }

    var text: String = ""
        set(value) {
            field = value
            categoryName.text = field
        }


    override fun setOnClickListener(l: OnClickListener?) {
        cardItem.setOnClickListener(l)
    }

    private fun init(context: Context) {
        inflate(context, R.layout.item_category, this)
    }
}