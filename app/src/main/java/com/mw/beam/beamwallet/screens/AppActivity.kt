package com.mw.beam.beamwallet.screens

import android.content.Intent
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.DelayedTask
import kotlinx.android.synthetic.main.activity_app.*
import java.util.*

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

    override fun addListeners() {
        btnUndoSent.setOnClickListener {
            presenter?.onUndoSend()
        }
    }

    override fun clearListeners() {
        btnUndoSent.setOnClickListener(null)
    }

    fun pendingSend(info: PendingSendInfo) {
        presenter?.onPendingSend(info)
    }

    fun startNewSnackbar() {
        undoSentCard.visibility = View.VISIBLE
    }

    fun cancelSnackbar() {
        undoSentCard.visibility = View.GONE
    }

    fun updateSnackbar(second: Int) {
        undoTime.text = second.toString()
    }

    override fun ensureState(): Boolean = true

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppActivityPresenter(this, AppActivityRepository())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        findNavController(R.id.nav_host)
    }
}

data class PendingSendInfo(val token: String, val comment: String?, val amount: Long, val fee: Long) {
    val id by lazy {
        UUID.randomUUID().toString()
    }
}

class AppActivityPresenter(view: AppActivity?, repository: AppActivityRepository) : BasePresenter<AppActivity, AppActivityRepository>(view, repository) {
    private val duration = 5
    private var currentDelayedTask: DelayedTask? = null
    private var currentPendingInfo: PendingSendInfo? = null

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
            view?.cancelSnackbar()
        }
    }

    fun onPendingSend(info: PendingSendInfo) {
        view?.cancelSnackbar()
        view?.startNewSnackbar()
        currentPendingInfo = info
        currentDelayedTask = DelayedTask.startNew(
                duration,
                {
                    repository.sendMoney(info.token, info.comment, info.amount, info.fee)
                    if (info.id == currentPendingInfo?.id) {
                        view?.cancelSnackbar()
                    }
                },
                { view?.updateSnackbar(it) },
                { if (info.id == currentPendingInfo?.id) view?.cancelSnackbar() }
        )
    }

    fun onUndoSend() {
        currentDelayedTask?.cancel(true)
        view?.cancelSnackbar()
    }
}

class AppActivityRepository: BaseRepository() {
    fun sendMoney(token: String, comment: String?, amount: Long, fee: Long) {
        getResult("sendMoney", " token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {
            wallet?.sendMoney(token, comment, amount, fee)
        }
    }
}