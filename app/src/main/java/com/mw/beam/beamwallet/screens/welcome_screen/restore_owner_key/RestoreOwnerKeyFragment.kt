package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

class RestoreOwnerKeyFragment: BaseFragment<RestoreOwnerKeyPresenter>(), RestoreOwnerKeyContract.View {

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_owner_key

    override fun getToolbarTitle(): String? = getString(R.string.owner_key)

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreOwnerKeyPresenter(this, RestoreOwnerKeyRepository())
    }
}