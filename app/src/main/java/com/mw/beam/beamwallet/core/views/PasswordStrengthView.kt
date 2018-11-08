package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.mw.beam.beamwallet.R
import kotlinx.android.synthetic.main.password_strength.view.*

/**
 * Created by vain onnellinen on 10/23/18.
 */
class PasswordStrengthView : ConstraintLayout {
    var strength: Strength = Strength.EMPTY
        set(value) {
            field = value
            configLevel(strength)
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
        ConstraintLayout.inflate(context, R.layout.password_strength, this)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.PasswordStrengthView,
                    0, 0
            )

            strength = Strength.fromValue(a.getResourceId(R.styleable.PasswordStrengthView_strength, 0))
        }
    }

    private fun configLevel(strength: Strength) {
        veryWeak.setImageLevel(strength.value)
        weak.setImageLevel(
                when (strength) {
                    Strength.VERY_WEAK -> Strength.EMPTY.value
                    else -> strength.value
                })
        medium.setImageLevel(
                when (strength) {
                    Strength.VERY_WEAK, Strength.WEAK -> Strength.EMPTY.value
                    else -> strength.value
                })
        mediumStrong.setImageLevel(
                when (strength) {
                    Strength.VERY_WEAK, Strength.WEAK, Strength.MEDIUM -> Strength.EMPTY.value
                    else -> strength.value
                })
        strong.setImageLevel(
                when (strength) {
                    Strength.EMPTY, Strength.STRONG, Strength.VERY_STRONG -> strength.value
                    else -> Strength.EMPTY.value
                })
        veryStrong.setImageLevel(
                when (strength) {
                    Strength.EMPTY, Strength.VERY_STRONG -> strength.value
                    else -> Strength.EMPTY.value
                })
    }

    enum class Strength(val value: Int) {
        EMPTY(0), VERY_WEAK(1), WEAK(2), MEDIUM(3), MEDIUM_STRONG(4), STRONG(5), VERY_STRONG(6);

        companion object {
            private val map: HashMap<Int, Strength> = HashMap()

            init {
                Strength.values().forEach {
                    map[it.value] = it
                }
            }

            fun fromValue(type: Int): Strength {
                return map[type] ?: throw IllegalArgumentException("Unknown type of progress")
            }
        }
    }
}
