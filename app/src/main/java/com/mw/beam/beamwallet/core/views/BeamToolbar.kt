package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mw.beam.beamwallet.R

/**
 * Created by vain onnellinen on 12/10/18.
 */
class BeamToolbar : ConstraintLayout {
    var title: String? = null
        set(value) {
            field = value
            if (field != null) {
                toolbarTitle.text = field
            }
        }
    var hasStatus: Boolean = false
        set(value) {
            field = value
            status.visibility = if (field) View.VISIBLE else View.GONE
            statusIcon.visibility = if (field) View.VISIBLE else View.GONE
        }
    lateinit var toolbar: Toolbar
    lateinit var toolbarTitle: TextView
    lateinit var status: TextView
    lateinit var statusIcon: ImageView

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
        inflate(context, R.layout.toolbar, this)
        toolbar = this.findViewById(R.id.toolbar)
        toolbarTitle = toolbar.findViewById(R.id.toolbarTitle)
        status = toolbar.findViewById(R.id.status)
        statusIcon = toolbar.findViewById(R.id.statusIcon)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.BeamToolbar,
                    0, 0
            )

            title = a.getNonResourceString(R.styleable.BeamToolbar_title)
            hasStatus = a.getBoolean(R.styleable.BeamToolbar_hasStatus, false)
        }
    }
}
