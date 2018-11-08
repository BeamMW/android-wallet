package com.mw.beam.beamwallet.welcomeScreen.welcomeRecover

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*
import kotlinx.android.synthetic.main.fragment_welcome_recover.*

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRecoverFragment : BaseFragment<WelcomeRecoverPresenter>(), WelcomeRecoverContract.View {
    private lateinit var presenter: WelcomeRecoverPresenter
    private var sideOffset: Int = Int.MIN_VALUE
    private var topOffset: Int = Int.MIN_VALUE

    companion object {

        fun newInstance(): WelcomeRecoverFragment {
            val args = Bundle()
            val fragment = WelcomeRecoverFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomeRecoverFragment::class.java.simpleName
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_welcome_recover, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = WelcomeRecoverPresenter(this, WelcomeRecoverRepository())
        configPresenter(presenter)
    }

    override fun init() {
        sideOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        topOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)

        btnRecover.isEnabled = false
        btnRecover.setOnClickListener {
            if (it.isEnabled) {
                presenter.onRecoverPressed()
            }
        }
    }

    override fun configPhrases(phrasesCount: Int) {
        phrasesLayout.rowCount = phrasesCount / 2
        var columnIndex = 0
        var rowIndex = 0

        for (i in 1..phrasesCount) {
            if (columnIndex == phrasesLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            phrasesLayout.addView(configPhrase(i, rowIndex, columnIndex))
            columnIndex++
        }
    }

    private fun configPhrase(number: Int, rowIndex: Int, columnIndex: Int): View? {
        val context = context ?: return null

        val phrase = BeamPhraseInput(context)
        phrase.number = number

        val params = GridLayout.LayoutParams()
        params.height = GridLayout.LayoutParams.WRAP_CONTENT
        params.width = GridLayout.LayoutParams.WRAP_CONTENT
        params.columnSpec = GridLayout.spec(columnIndex, 1f)
        params.rowSpec = GridLayout.spec(rowIndex)
        params.topMargin = topOffset

        when (columnIndex) {
            0 -> params.rightMargin = sideOffset
            1 -> params.leftMargin = sideOffset
        }

        phrase.layoutParams = params

        phrase.phraseView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                presenter.onPhraseChanged()
            }
        })

        return phrase
    }

    override fun handleRecoverButton() {
        btnRecover.isEnabled = arePhrasesFilled()
    }

    private fun arePhrasesFilled(): Boolean {
        for (i in 0 until phrasesLayout.childCount) {
            if ((phrasesLayout.getChildAt(i) as BeamPhraseInput).phraseView.text.isNullOrBlank()) {
                return false
            }
        }

        return true
    }
}
