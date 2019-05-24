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

    override fun getToolbarTitle(): String? = getString(R.string.language_title)

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
                getString(R.string.common_cancel),
                {},
                null,
                getString(R.string.language_restart_now),
                {
                    adapter.setSelected(languageIndex)
                    presenter?.onRestartPressed(languageIndex)
                })
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return LanguagePresenter(this, LanguageRepository())
    }
}