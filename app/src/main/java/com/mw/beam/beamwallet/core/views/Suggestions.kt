package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.mw.beam.beamwallet.R
import kotlinx.android.synthetic.main.suggestion_layout.view.*


class Suggestions: LinearLayout {
    private var suggestions: List<String>? = null
    private var onSuggestionClick: OnSuggestionClick? = null

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
        View.inflate(context, R.layout.suggestion_layout, this)

        val onClickListener = OnTextSuggestionClickListener(this)

        leftWord.setOnClickListener(onClickListener)
        centerWord.setOnClickListener(onClickListener)
        rightWord.setOnClickListener(onClickListener)
    }

    fun setSuggestions(suggestions: List<String>) {
        this.suggestions = suggestions
    }

    fun clear() {
        leftWord.text = ""
        centerWord.text = ""
        rightWord.text = ""

        updateDividers()
    }

    fun find(text: String) {
        clear()

        if (text.isEmpty()) {
            return
        }

        val words = suggestions?.filter { it.startsWith(text) }?.take(3)

        words?.forEach {
            val textView = when (words.indexOf(it)) {
                1 -> leftWord
                2 -> rightWord
                else -> centerWord
            }

            textView.text = it
        }

        updateDividers()
    }

    fun contains(text: String): Boolean = suggestions?.contains(text) ?: false

    private fun updateDividers() {
        view.visibility = if (leftWord.text.isEmpty())  View.GONE else View.VISIBLE
        view2.visibility = if (rightWord.text.isEmpty())  View.GONE else View.VISIBLE
    }

    fun setOnSuggestionClick(onSuggestionClick: OnSuggestionClick?) {
        this.onSuggestionClick = onSuggestionClick
    }

    private class OnTextSuggestionClickListener(private val suggestions: Suggestions): OnClickListener {
        override fun onClick(v: View?) {
            if (v != null && v is TextView && v.text.isNotEmpty()) {
                suggestions.onSuggestionClick?.onClick(v.text.toString())
            }
        }

    }
}

interface OnSuggestionClick {
    fun onClick(suggestion: String)
}