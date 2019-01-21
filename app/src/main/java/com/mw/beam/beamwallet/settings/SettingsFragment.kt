package com.mw.beam.beamwallet.settings

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {
    private lateinit var presenter: SettingsPresenter

    companion object {
        fun newInstance() = SettingsFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = SettingsFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_settings
    override fun getToolbarTitle(): String? = getString(R.string.settings_title)

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SettingsPresenter(this, SettingsRepository())
        return presenter
    }
}
