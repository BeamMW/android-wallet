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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_seed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.views.BeamPhrase
import kotlinx.android.synthetic.main.fragment_welcome_seed.*


/**
 * Created by vain onnellinen on 10/30/18.
 */
class WelcomeSeedFragment : BaseFragment<WelcomeSeedPresenter>(), WelcomeSeedContract.View {
    private val COPY_TAG = "RECOVERY SEED"
    private lateinit var presenter: WelcomeSeedPresenter
    private lateinit var copiedAlert: String

    companion object {
        fun newInstance() = WelcomeSeedFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WelcomeSeedFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_seed
    override fun getToolbarTitle(): String? = getString(R.string.welcome_seed_title)

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)

        copiedAlert = getString(R.string.welcome_seed_copied_alert)
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

    override fun configSeed(seed: Array<String>) {
        val sideOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_side_offset)
        val topOffset: Int = resources.getDimensionPixelSize(R.dimen.welcome_grid_element_top_offset)
        var columnIndex = 0
        var rowIndex = 0
        seedLayout.rowCount = seed.size / 2

        for ((index, value) in seed.withIndex()) {
            if (columnIndex == seedLayout.columnCount) {
                columnIndex = 0
                rowIndex++
            }

            seedLayout.addView(configPhrase(value, index + 1, rowIndex, columnIndex, sideOffset, topOffset))
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
        showAlert(getString(R.string.welcome_seed_save_description), getString(R.string.welcome_seed_save_title),
                getString(R.string.common_done), getString(R.string.common_cancel),
                { presenter.onDonePressed() })
    }

    override fun showConfirmFragment(seed: Array<String>) = (activity as SeedHandler).proceedToValidation(seed)

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
        presenter = WelcomeSeedPresenter(this, WelcomeSeedRepository())
        return presenter
    }

    interface SeedHandler {
        fun proceedToValidation(seed: Array<String>)
    }
}
