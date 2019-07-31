package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

class RestoreTustedNodeFragment : BaseFragment<RestoreTustedNodePresenter>(), RestoreTustedNodeContract.View {

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_trusted_node

    override fun getToolbarTitle(): String? = getString(R.string.restore_wallet)

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreTustedNodePresenter(this, RestoreTustedNodeRepository())
    }
}