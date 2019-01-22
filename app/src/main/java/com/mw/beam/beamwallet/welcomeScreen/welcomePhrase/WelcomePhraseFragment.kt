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

package com.mw.beam.beamwallet.welcomeScreen.welcomePhrase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.views.BeamPhrase
import kotlinx.android.synthetic.main.fragment_welcome_phrase.*


/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhraseFragment : BaseFragment<WelcomePhrasePresenter>(), WelcomePhraseContract.View {
    private val COPY_TAG = "RECOVERY PHRASES"
    private lateinit var presenter: WelcomePhrasePresenter
    private lateinit var copiedAlert: String

    companion object {
        fun newInstance() = WelcomePhraseFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomePhraseFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_phrase
    override fun getToolbarTitle(): String? = getString(R.string.welcome_phrase_title)

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)

        copiedAlert = getString(R.string.welcome_phrase_copied_alert)
    }

    override fun addListeners() {
        btnNext.setOnClickListener {
            presenter.onNextPressed()
        }

        btnCopy.setOnClickListener {
            presenter.onCopyPressed()
        }
    }

    override fun clearListeners() {
        btnNext.setOnClickListener(null)
        btnCopy.setOnClickListener(null)
    }

    override fun configPhrases(phrases: Array<String>) {
        val sideOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        val topOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)
        var columnIndex = 0
        var rowIndex = 0
        phrasesLayout.rowCount = phrases.size / 2

        for ((index, value) in phrases.withIndex()) {
            if (columnIndex == phrasesLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            phrasesLayout.addView(configPhrase(value, index + 1, rowIndex, columnIndex, sideOffset, topOffset))
            columnIndex++
        }
    }

    override fun copyToClipboard(data: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(COPY_TAG, data)
    }

    override fun showCopiedAlert() {
        showSnackBar(copiedAlert)
    }

    override fun showSaveAlert() {
        showAlert(getString(R.string.welcome_phrase_save_description), getString(R.string.welcome_phrase_save_title),
                getString(R.string.common_done), getString(R.string.common_cancel),
                { presenter.onDonePressed() })
    }

    override fun showValidationFragment(phrases: Array<String>) = (activity as WelcomePhrasesHandler).proceedToValidation(phrases)

    private fun configPhrase(text: String, number: Int, rowIndex: Int, columnIndex: Int, sideOffset: Int, topOffset: Int): View? {
        val context = context ?: return null

        val phrase = BeamPhrase(context)
        phrase.phrase = text
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

        return phrase
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomePhrasePresenter(this, WelcomePhraseRepository())
        return presenter
    }

    interface WelcomePhrasesHandler {
        fun proceedToValidation(phrases: Array<String>)
    }
}
