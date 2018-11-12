package com.mw.beam.beamwallet.welcomeScreen.welcomeValidation

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Phrases
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*
import kotlinx.android.synthetic.main.fragment_welcome_validation.*

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeValidationFragment : BaseFragment<WelcomeValidationPresenter>(), WelcomeValidationContract.View {
    private lateinit var presenter: WelcomeValidationPresenter
    private var sideOffset: Int = Int.MIN_VALUE
    private var topOffset: Int = Int.MIN_VALUE

    companion object {
        private const val ARG_PHRASES = "ARG_PHRASES"

        fun newInstance(phrases: MutableList<String>): WelcomeValidationFragment {
            val args = Bundle()
            //TODO hack - replace by appropriate data class when implemented
            args.putParcelable(ARG_PHRASES, Phrases(phrases))
            val fragment = WelcomeValidationFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomeValidationFragment::class.java.simpleName
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_validation

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)

        sideOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        topOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)
    }

    override fun onControllerStart() {
        super.onControllerStart()
        btnNext.isEnabled = false
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            if (it.isEnabled) {
                presenter.onNextPressed()
            }
        }
    }

    private fun arePhrasesValid(): Boolean {
        for (i in 0 until phrasesLayout.childCount) {
            if (!(phrasesLayout.getChildAt(i) as BeamPhraseInput).isValid) {
                return false
            }
        }

        return true
    }

    override fun getData(): Phrases? = arguments?.getParcelable(ARG_PHRASES)
    override fun showPasswordsFragment() = (activity as WelcomeValidationHandler).proceedToPasswords()

    override fun handleNextButton() {
        btnNext.isEnabled = arePhrasesValid()
    }

    override fun configPhrases(phrasesToValidate: MutableList<Int>, phrases: Phrases) {
        phrasesLayout.rowCount = phrasesToValidate.size / 2
        var columnIndex = 0
        var rowIndex = 0

        for (phraseNumber in phrasesToValidate) {
            if (columnIndex == phrasesLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            phrasesLayout.addView(configPhrase(phraseNumber, rowIndex, columnIndex, phrases.phrases[phraseNumber - 1]))
            columnIndex++
        }
    }

    private fun configPhrase(number: Int, rowIndex: Int, columnIndex: Int, validPhrase: String): View? {
        val context = context ?: return null

        val phrase = BeamPhraseInput(context)
        phrase.number = number
        phrase.phrase = validPhrase
        phrase.isForEnsure = true

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

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomeValidationPresenter(this, WelcomeValidationRepository())
        return presenter
    }

    interface WelcomeValidationHandler {
        fun proceedToPasswords()
    }
}
