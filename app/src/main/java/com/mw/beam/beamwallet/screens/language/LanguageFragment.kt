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

package com.mw.beam.beamwallet.screens.language

import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import kotlinx.android.synthetic.main.fragment_language.*

class LanguageFragment: BaseFragment<LanguagePresenter>(), LanguageContract.View {
    private lateinit var adapter: LanguageAdapter

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_language

    override fun getToolbarTitle(): String? = getString(R.string.language)

    override fun init(languages: List<String>, currentLanguage: Int) {
        adapter = LanguageAdapter(languages) {
            presenter?.onSelectLanguage(it)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter.setSelected(currentLanguage)
    }

    override fun showConfirmDialog(languageIndex: Int) {
        showAlert(
                getString(R.string.language_dialog_message),
                getString(R.string.cancel),
                {},
                null,
                getString(R.string.restart_now),
                {
                    adapter.setSelected(languageIndex)
                    presenter?.onRestartPressed(languageIndex)
                })
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return LanguagePresenter(this, LanguageRepository())
    }
}