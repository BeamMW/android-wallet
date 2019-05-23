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
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R

/**
 * Created by vain onnellinen on 12/4/18.
 */
class BeamEditText : AppCompatEditText {
    companion object {
        private val STATE_ACCENT = intArrayOf(R.attr.state_accent)
        private val STATE_NORMAL = intArrayOf(R.attr.state_normal)
        private val STATE_ERROR = intArrayOf(R.attr.state_error)
    }

    var isStateAccent: Boolean = false
        set(value) {
            field = value
            if (field) {
                isStateNormal = false
                isStateError = false
                refreshDrawableState()
            }
        }
    var isStateNormal: Boolean = false
        set(value) {
            field = value
            if (field) {
                isStateAccent = false
                isStateError = false
                refreshDrawableState()
            }
        }
    var isStateError: Boolean = false
        set(value) {
            field = value
            if (field) {
                isStateAccent = false
                isStateNormal = false
                refreshDrawableState()
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)

        when {
            isStateAccent -> mergeDrawableStates(drawableState, STATE_ACCENT)
            isStateNormal -> mergeDrawableStates(drawableState, STATE_NORMAL)
            isStateError -> mergeDrawableStates(drawableState, STATE_ERROR)
        }

        return drawableState
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.background = ContextCompat.getDrawable(context, R.drawable.edit_text_selector)
        setTextColor(ContextCompat.getColorStateList(context, R.color.text_color_selector))

        this.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                isStateAccent = true
            } else {
                isStateNormal = true
            }
        }

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.BeamEditText,
                    0, 0
            )

            isStateAccent = a.getBoolean(R.styleable.BeamEditText_state_accent, false)
            isStateNormal = a.getBoolean(R.styleable.BeamEditText_state_normal, false)
            isStateError = a.getBoolean(R.styleable.BeamEditText_state_error, false)
        }
    }
}
