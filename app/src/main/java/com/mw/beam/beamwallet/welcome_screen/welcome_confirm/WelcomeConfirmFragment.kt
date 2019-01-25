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

package com.mw.beam.beamwallet.welcome_screen.welcome_confirm

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*
import kotlinx.android.synthetic.main.fragment_welcome_confirm.*

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmFragment : BaseFragment<WelcomeConfirmPresenter>(), WelcomeConfirmContract.View {
    private lateinit var presenter: WelcomeConfirmPresenter
    private var sideOffset: Int = Int.MIN_VALUE
    private var topOffset: Int = Int.MIN_VALUE

    companion object {
        private const val ARG_PHRASES = "ARG_PHRASES"

        fun newInstance(phrases: Array<String>) = WelcomeConfirmFragment().apply { arguments = Bundle().apply { putStringArray(ARG_PHRASES, phrases) } }
        fun getFragmentTag(): String = WelcomeConfirmFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_confirm
    override fun getToolbarTitle(): String =getString(R.string.welcome_validation_title)

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

    override fun getData(): Array<String>? = arguments?.getStringArray(ARG_PHRASES)
    override fun showPasswordsFragment(phrases: Array<String>) = (activity as WelcomeValidationHandler).proceedToPasswords(phrases)

    override fun handleNextButton() {
        btnNext.isEnabled = arePhrasesValid()
    }

    override fun configPhrases(phrasesToValidate: List<Int>, phrases: Array<String>) {
        phrasesLayout.rowCount = phrasesToValidate.size / 2
        var columnIndex = 0
        var rowIndex = 0

        for (phraseNumber in phrasesToValidate) {
            if (columnIndex == phrasesLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            phrasesLayout.addView(configPhrase(phraseNumber, rowIndex, columnIndex, phrases[phraseNumber - 1]))
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

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeConfirmPresenter(this, WelcomeConfirmRepository())
        return presenter
    }

    interface WelcomeValidationHandler {
        fun proceedToPasswords(phrases: Array<String>)
    }
}
