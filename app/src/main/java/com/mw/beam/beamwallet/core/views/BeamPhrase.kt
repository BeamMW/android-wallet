package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.mw.beam.beamwallet.R
import kotlinx.android.synthetic.main.common_phrase.view.*

/**
 * Created by vain onnellinen on 10/30/18.
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
