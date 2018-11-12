package com.mw.beam.beamwallet.welcomeScreen.welcomePhrases

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.views.BeamPhrase
import kotlinx.android.synthetic.main.fragment_welcome_phrases.*


/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomePhrasesFragment : BaseFragment<WelcomePhrasesPresenter>(), WelcomePhrasesContract.View {
    private val COPY_TAG = "RECOVERY PHRASES"
    private lateinit var presenter: WelcomePhrasesPresenter
    private var sideOffset: Int = Int.MIN_VALUE
    private var topOffset: Int = Int.MIN_VALUE

    companion object {
        fun newInstance(): WelcomePhrasesFragment {
            val args = Bundle()
            val fragment = WelcomePhrasesFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WelcomePhrasesFragment::class.java.simpleName
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_phrases

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)

        sideOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        topOffset = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)
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

    override fun configPhrases(phrases: MutableList<String>) {
        phrasesLayout.rowCount = phrases.size / 2
        var columnIndex = 0
        var rowIndex = 0

        for ((index, value) in phrases.withIndex()) {
            if (columnIndex == phrasesLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            phrasesLayout.addView(configPhrase(value, index + 1, rowIndex, columnIndex))
            columnIndex++
        }
    }

    override fun copyToClipboard(data: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(COPY_TAG, data)
    }

    override fun showValidationFragment(phrases: MutableList<String>) = (activity as WelcomePhrasesHandler).proceedToValidation(phrases)

    private fun configPhrase(text: String, number: Int, rowIndex: Int, columnIndex: Int): View? {
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

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WelcomePhrasesPresenter(this, WelcomePhrasesRepository())
        return presenter
    }

    interface WelcomePhrasesHandler {
        fun proceedToValidation(phrases: MutableList<String>)
    }
}
