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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import kotlinx.android.synthetic.main.common_phrase.view.*

/**
 *  10/30/18.
 */
class BeamPhrase : ConstraintLayout {
    var phrase: String = ""
        set(value) {
            field = value
            if (field != "") {
                phraseView.text = field
            }
        }
    var number: Int = Int.MIN_VALUE
        set(value) {
            field = value
            if (field != Int.MIN_VALUE) {
                numberView.text = field.toString()
            }
        }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.common_phrase, this)
        setPadding(resources.getDimensionPixelSize(R.dimen.welcome_number_offset), 0, 0, 0)
        background = ContextCompat.getDrawable(context, R.drawable.common_phrase)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.BeamPhrase,
                    0, 0
            )

            phrase = a.getNonResourceString(R.styleable.BeamPhrase_phrase)
            number = a.getResourceId(R.styleable.BeamPhrase_number, Integer.MIN_VALUE)
        }
    }
}
