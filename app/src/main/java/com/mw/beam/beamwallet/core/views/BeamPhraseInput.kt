package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.util.AttributeSet
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*

/**
 * Created by vain onnellinen on 11/1/18.
 */
class BeamPhraseInput : ConstraintLayout {
    var phrase: String = ""
    var isForEnsure: Boolean = false
    var isValid: Boolean = false
        get() = phrase == phraseView.text?.toString()?.trim()
    var number: Int = Int.MIN_VALUE
        set(value) {
            field = value
            if (field != Int.MIN_VALUE) {
                numberView.text = field.toString()
            }
        }
    var numberTextColorResId: Int = Integer.MIN_VALUE
        set(value) {
            field = value
            if (field != Integer.MIN_VALUE) {
                numberView.setTextColor(ContextCompat.getColor(context, field))
            }
        }
    var numberBackgroundResId: Int = Integer.MIN_VALUE
        set(value) {
            field = value
            if (field != Integer.MIN_VALUE) {
                numberView.background = ContextCompat.getDrawable(context, field)
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
        inflate(context, R.layout.common_phrase_input, this)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.BeamPhraseInput,
                    0, 0
            )

            phrase = a.getNonResourceString(R.styleable.BeamPhrase_phrase)
            number = a.getResourceId(R.styleable.BeamPhraseInput_number, Integer.MIN_VALUE)
            isForEnsure = a.getBoolean(R.styleable.BeamPhraseInput_isForEnsure, false)
            numberTextColorResId = a.getResourceId(R.styleable.BeamPhraseInput_number_text_color, Integer.MIN_VALUE)
            numberBackgroundResId = a.getResourceId(R.styleable.BeamPhraseInput_number_background, Integer.MIN_VALUE)
        }

        phraseView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (isForEnsure) {
                    if (isValid) {
                        numberTextColorResId = R.color.phrase_validated_number_text_color
                        numberBackgroundResId = R.drawable.validated_number_background
                    } else {
                        numberTextColorResId = R.color.colorPrimary
                        numberBackgroundResId = R.drawable.empty_number_background
                    }
                } else {
                    if (phraseView.text?.toString()?.trim().isNullOrBlank()) {
                        numberTextColorResId = R.color.phrase_number_text_color
                        numberBackgroundResId = R.drawable.empty_number_background
                    } else {
                        numberTextColorResId = R.color.phrase_number_text_color
                        numberBackgroundResId = R.drawable.number_background
                    }
                }
            }
        })
    }
}
