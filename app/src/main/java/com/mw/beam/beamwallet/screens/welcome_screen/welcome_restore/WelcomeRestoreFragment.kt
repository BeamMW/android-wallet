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

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.views.OnSuggestionClick
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*
import kotlinx.android.synthetic.main.fragment_welcome_restore.*

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestoreFragment : BaseFragment<WelcomeRestorePresenter>(), WelcomeRestoreContract.View {
    private lateinit var presenter: WelcomeRestorePresenter
    private var currentEditText: EditText? = null

    companion object {
        fun newInstance() = WelcomeRestoreFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeRestoreFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_restore
    override fun getToolbarTitle(): String = getString(R.string.welcome_restore_title)

    override fun init() {
        btnRestore.isEnabled = false
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
    }

    override fun addListeners() {
        btnRestore.setOnClickListener {
            if (it.isEnabled) {
                presenter.onRestorePressed()
            }
        }

        suggestionsView.setOnSuggestionClick(object: OnSuggestionClick {
            override fun onClick(suggestion: String) {
                presenter.onSuggestionClick(suggestion)
            }
        })

        addKeyboardStateListener(restoreRootLayout)
    }

    override fun onHideKeyboard() {
        presenter.onKeyboardStateChange(false)
    }

    override fun onShowKeyboard(keyboardHeight: Int) {
        presenter.onKeyboardStateChange(true)
    }

    override fun showPasswordsFragment(seed: Array<String>) = (activity as RestoreHandler).proceedToPasswords(seed, WelcomeMode.RESTORE)

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
        (seedLayout.getChildAt(phrasesCount - 1) as BeamPhraseInput).phraseView.imeOptions = EditorInfo.IME_ACTION_DONE

        (seedLayout.getChildAt(0) as BeamPhraseInput).requestFocus()
        showKeyboard()
    }

    private fun configPhrase(number: Int, rowIndex: Int, columnIndex: Int, sideOffset: Int, topOffset: Int): View? {
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
                presenter.onSeedChanged(p0.toString())
            }
        })

        phrase.phraseView.setOnFocusChangeListener { v, hasFocus ->
            presenter.onSeedFocusChanged((v as EditText?)?.text.toString(), hasFocus)
            if (hasFocus) {
                currentEditText = v
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
            seed.add((seedLayout.getChildAt(i) as BeamPhraseInput).phraseView.text.toString())
        }

        return seed.toTypedArray()
    }

    private fun arePhrasesFilled(): Boolean {
        for (i in 0 until seedLayout.childCount) {
            if ((seedLayout.getChildAt(i) as BeamPhraseInput).phraseView.text.isNullOrBlank()) {
                return false
            }
        }

        return true
    }

    override fun clearListeners() {
        btnRestore.setOnClickListener(null)
        suggestionsView.setOnSuggestionClick(null)
        clearKeyboardStateListener()
    }

    override fun clearWindowState() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeRestorePresenter(this, WelcomeRestoreRepository(), WelcomeRestoreState())
        return presenter
    }

    interface RestoreHandler {
        fun proceedToPasswords(seed: Array<String>, mode: WelcomeMode)
    }
}
