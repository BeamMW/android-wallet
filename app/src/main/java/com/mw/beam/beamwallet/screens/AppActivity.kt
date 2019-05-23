package com.mw.beam.beamwallet.screens

import android.content.Intent
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.App

class AppActivity : BaseActivity<AppActivityPresenter>() {


    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_app

    override fun getToolbarTitle(): String? = null

    fun showOpenFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.welcomeOpenFragment, null, navOptions {
            popUpTo(R.id.navigation) {}
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        presenter?.onNewIntent()
    }

    fun showWalletFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.walletFragment, null, navOptions {
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

    fun onNewIntent() {
        if (App.isAuthenticated) {
            view?.showWalletFragment()
        } else {
            view?.showOpenFragment()
        }
    }
}

class AppActivityRepository: BaseRepository()