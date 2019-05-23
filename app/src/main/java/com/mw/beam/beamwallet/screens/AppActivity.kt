package com.mw.beam.beamwallet.screens

import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*

class AppActivity : BaseActivity<AppActivityPresenter>() {


    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_app

    override fun getToolbarTitle(): String? = null

    fun showOpenFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.welcomeOpenFragment, null, navOptions {
            popUpTo(R.id.navigation) {}
        })
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppActivityPresenter(this, AppActivityRepository())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        findNavController(R.id.nav_host)
    }
}

class AppActivityPresenter(view: AppActivity?, repository: AppActivityRepository) : BasePresenter<AppActivity, AppActivityRepository>(view, repository) {
    override fun onViewCreated() {
        super.onViewCreated()
        if (repository.isWalletInitialized()) {
            view?.showOpenFragment()
        }
    }
}

class AppActivityRepository: BaseRepository()