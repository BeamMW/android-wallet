package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
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
            field = value ?: R.color.colorAccent

            colorCircle.setCardBackgroundColor(resources.getColor(field!!, App.self.theme))
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