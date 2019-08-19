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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_restore

import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.BeamEditText
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.views.OnSuggestionClick
import com.mw.beam.beamwallet.core.views.Suggestions
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.fragment_welcome_restore.*
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.PasteManager


/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestoreFragment : BaseFragment<WelcomeRestorePresenter>(), WelcomeRestoreContract.View {
    private var currentEditText: EditText? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_restore
    override fun getToolbarTitle(): String = getString(R.string.restore_wallet)

    override fun init() {
        btnRestore.isEnabled = false

        when(BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MASTERNET -> {btnShare.visibility = View.VISIBLE}
            AppConfig.FLAVOR_TESTNET -> {btnShare.visibility = View.VISIBLE}
            AppConfig.FLAVOR_MAINNET -> {btnShare.visibility = View.GONE}
        }
    }

    override fun initSuggestions(suggestions: List<String>) {
        suggestionsView.setSuggestions(suggestions)
    }

    override fun clearSuggestions() {
        suggestionsView.clear()
    }

    override fun showSuggestions() {
        suggestionsView.visibility = View.VISIBLE
    }

    override fun hideSuggestions() {
        suggestionsView.visibility = View.GONE
    }

    override fun setTextToCurrentView(text: String) {
        currentEditText?.apply {
            setText("")
            append(text)
            onEditorAction(imeOptions)
        }
    }

    override fun updateSuggestions(text: String) {
        suggestionsView.find(text)
        suggestionsView.mode = Suggestions.Mode.SingleWord
    }

    override fun addListeners() {
        btnShare.setOnClickListener {
            val data = PasteManager.getPasteData(context)
            val phrases1 = data.split(";").toTypedArray()
            val phrases2 = data.split("\n").toTypedArray()

            if (phrases1.count() == seedLayout.childCount)
            {
                for (i in 0 until seedLayout.childCount) {
                    val phraseInput = seedLayout.getChildAt(i) as BeamPhraseInput
                    phraseInput.editText.apply {
                        setText(phrases1[i])
                        append(text)
                        onEditorAction(imeOptions)
                    }
                }
            }

            if (phrases2.count() == seedLayout.childCount)
            {
                for (i in 0 until seedLayout.childCount) {
                    val phrase = phrases2[i].split(" ").toTypedArray().last()
                    val phraseInput = seedLayout.getChildAt(i) as BeamPhraseInput
                    phraseInput.editText.apply {
                        setText(phrase)
                        onEditorAction(imeOptions)
                    }
                }
            }

            btnRestore.isEnabled = arePhrasesFilled()
        }

        btnRestore.setOnClickListener {
            if (it.isEnabled) {
                presenter?.onRestorePressed()
            }
        }

        suggestionsView.setOnSuggestionClick(object: OnSuggestionClick {
            override fun onClick(suggestion: String) {
                presenter?.onSuggestionClick(suggestion)
            }
        })
    }

    override fun onHideKeyboard() {
        presenter?.onKeyboardStateChange(false)
    }

    override fun onShowKeyboard() {
        presenter?.onKeyboardStateChange(true)

        if (currentEditText != null)
        {
            val view = seedLayout.findFocus() as BeamEditText
            val rowIndex = view.tag as Int
            if (rowIndex > 3) {
                val y = view.height * rowIndex + suggestionsView.height
                mainScroll.smoothScrollTo(0, y)
            }
        }
    }

    override fun showPasswordsFragment(seed: Array<String>) {
        findNavController().navigate(WelcomeRestoreFragmentDirections.actionWelcomeRestoreFragmentToPasswordFragment(seed, WelcomeMode.RESTORE.name))
    }

    override fun configSeed(phrasesCount: Int) {
        val sideOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        val topOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)
        var columnIndex = 0
        var rowIndex = 0
        seedLayout.rowCount = phrasesCount / 2

        for (i in 1..phrasesCount) {
            if (columnIndex == seedLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            seedLayout.addView(configPhrase(i, rowIndex, columnIndex, sideOffset, topOffset))
            columnIndex++
        }

        //hide keyboard at last phrase
        (seedLayout.getChildAt(phrasesCount - 1) as BeamPhraseInput).editText.imeOptions = EditorInfo.IME_ACTION_DONE

        (seedLayout.getChildAt(0) as BeamPhraseInput).requestFocus()
        showKeyboard()
    }

    private fun configPhrase(number: Int, rowIndex: Int, columnIndex: Int, sideOffset: Int, topOffset: Int): View? {
        val context = context ?: return null

        val phrase = BeamPhraseInput(context)
        phrase.number = number
        phrase.validator = { presenter?.onValidateSeed(it) ?: false }

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
        phrase.editText.tag = rowIndex

        phrase.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(phrase: Editable?) {
                presenter?.onSeedChanged(phrase.toString())
            }
        })

        phrase.editText.setOnFocusChangeListener { v, hasFocus ->
            presenter?.onSeedFocusChanged((v as EditText?)?.text.toString(), hasFocus)
            if (hasFocus) {
                currentEditText = v as EditText?

                val index = phrase.number/2
                if (index > 3) {
                    val y = phrase.height * index + suggestionsView.height
                    mainScroll.smoothScrollTo(0, y)
                }
            }
        }

        return phrase
    }

    override fun handleRestoreButton() {
        btnRestore.isEnabled = arePhrasesFilled()
    }

    override fun getSeed(): Array<String> {
        val seed = ArrayList<String>()

        for (i in 0 until seedLayout.childCount) {
            seed.add((seedLayout.getChildAt(i) as BeamPhraseInput).editText.text.toString().trim())
        }

        return seed.toTypedArray()
    }

    private fun arePhrasesFilled(): Boolean {
        for (i in 0 until seedLayout.childCount) {
            val phraseInput = seedLayout.getChildAt(i) as BeamPhraseInput
            if (phraseInput.editText.text.isNullOrBlank() || !phraseInput.isValid) {
                return false
            }
        }

        return true
    }

    override fun clearListeners() {
        btnRestore.setOnClickListener(null)
        btnShare.setOnClickListener(null)
        suggestionsView.setOnSuggestionClick(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WelcomeRestorePresenter(this, WelcomeRestoreRepository(), WelcomeRestoreState())
    }
}
