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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_confirm

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.BeamPhraseInput
import com.mw.beam.beamwallet.core.views.OnSuggestionClick
import com.mw.beam.beamwallet.core.views.Suggestions
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import kotlinx.android.synthetic.main.common_phrase_input.view.*
import kotlinx.android.synthetic.main.fragment_welcome_confirm.*

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmFragment : BaseFragment<WelcomeConfirmPresenter>(), WelcomeConfirmContract.View {
    private var currentEditText: EditText? = null
    private var sideOffset: Int = Int.MIN_VALUE
    private var topOffset: Int = Int.MIN_VALUE

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_confirm
    override fun getToolbarTitle(): String = getString(R.string.welcome_validation_title)

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            presenter?.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

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
                presenter?.onNextPressed()
            }
        }

        suggestionsView.setOnSuggestionClick(object: OnSuggestionClick {
            override fun onClick(suggestion: String) {
                presenter?.onSuggestionClick(suggestion)
            }
        })
    }

    private fun isSeedValid(): Boolean {
        for (i in 0 until seedLayout.childCount) {
            if (!(seedLayout.getChildAt(i) as BeamPhraseInput).isValid) {
                return false
            }
        }

        return true
    }

    override fun getData(): Array<String>? = arguments?.let { WelcomeConfirmFragmentArgs.fromBundle(it).seed }
    override fun showPasswordsFragment(seed: Array<String>) {
        findNavController().navigate(WelcomeConfirmFragmentDirections.actionWelcomeConfirmFragmentToPasswordFragment(seed, WelcomeMode.CREATE.name))
    }
    override fun showSeedFragment() {
        findNavController().navigate(WelcomeConfirmFragmentDirections.actionWelcomeConfirmFragmentToWelcomeSeedFragment())
    }

    override fun handleNextButton() {
        btnNext.isEnabled = isSeedValid()
    }

    override fun configSeed(seedToValidate: List<Int>, seed: Array<String>) {
        seedLayout.rowCount = seedToValidate.size / 2
        var columnIndex = 0
        var rowIndex = 0

        for (phraseNumber in seedToValidate) {
            if (columnIndex == seedLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            seedLayout.addView(configPhrase(phraseNumber, rowIndex, columnIndex, seed[phraseNumber - 1]))
            columnIndex++
        }

        //to hide keyboard at last phrase
        (seedLayout.getChildAt(seedToValidate.size - 1) as BeamPhraseInput).phraseView.imeOptions = EditorInfo.IME_ACTION_DONE

        (seedLayout.getChildAt(0) as BeamPhraseInput).requestFocus()
        showKeyboard()
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
            override fun afterTextChanged(phrase: Editable?) {
                presenter?.onSeedChanged(phrase.toString())
            }
        })

        phrase.phraseView.setOnFocusChangeListener { v, hasFocus ->
            presenter?.onSeedFocusChanged((v as EditText?)?.text.toString(), hasFocus)
            if (hasFocus) {
                currentEditText = v as EditText?
            }
        }

        return phrase
    }


    override fun initSuggestions(suggestions: List<String>) {
        suggestionsView.setSuggestions(suggestions)
        suggestionsView.mode = Suggestions.Mode.SingleWord
    }

    override fun clearSuggestions() {
        suggestionsView.clear()
    }

    override fun setTextToCurrentView(text: String) {
        currentEditText?.apply {
            setText("")
            append(text)
            onEditorAction(imeOptions)
        }
    }

    override fun showSuggestions() {
        suggestionsView.visibility = View.VISIBLE
    }

    override fun hideSuggestions() {
        suggestionsView.visibility = View.GONE
    }

    override fun onHideKeyboard() {
        presenter?.onKeyboardStateChange(false)
    }

    override fun onShowKeyboard() {
        presenter?.onKeyboardStateChange(true)
    }

    override fun updateSuggestions(text: String) {
        suggestionsView.find(text)
    }

    override fun showSeedAlert() {
        showAlert(message = getString(R.string.welcome_validation_return_seed_message),
                title = getString(R.string.pass_back_seed_title),
                btnConfirmText = getString(R.string.pass_return_seed_btn_create_new),
                btnCancelText = getString(R.string.cancel),
                onConfirm = { presenter?.onCreateNewSeed() })
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
        suggestionsView.setOnSuggestionClick(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WelcomeConfirmPresenter(this, WelcomeConfirmRepository())
    }

}
