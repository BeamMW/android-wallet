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
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import kotlinx.android.synthetic.main.color_selector.view.*

class ColorSelector: FrameLayout {
    private val backgroundDrawable: Drawable? by lazy {
        ContextCompat.getDrawable(App.self, R.drawable.color_selector_background)
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

    var isSelectedColor: Boolean = false
        set(value) {
            field = value
            if (field) {
                colorSelectorBackground.background = backgroundDrawable
            } else {
                colorSelectorBackground.background = null
            }
        }

    var colorResId: Int? = null
        set(value) {
            field = value ?: R.color.colorAccent

            colorSelectorCard.setCardBackgroundColor(resources.getColor(field!!, App.self.theme))
        }

    private fun init(context: Context) {
        inflate(context, R.layout.color_selector, this)

        isSelectedColor = false
    }
}
