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

package com.mw.beam.beamwallet.screens.app_activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.AnimBuilder
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.screens.transaction_details.TransactionDetailsFragmentArgs
import android.app.DownloadManager
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import com.mw.beam.beamwallet.core.RestoreManager
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk

class AppActivity : BaseActivity<AppActivityPresenter>(), AppActivityContract.View {

    companion object {
        const val TRANSACTION_ID = "TRANSACTION_ID"
        private const val RESTARTED = "appExceptionHandler_restarted"
        private const val LAST_EXCEPTION = "appExceptionHandler_lastException"
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_app

    override fun getToolbarTitle(): String? = null


    override fun showOpenFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.welcomeOpenFragment, null, navOptions {
            popUpTo(R.id.navigation) { inclusive = true }
            launchSingleTop = true
            anim(buildTransitionAnimation())
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.isAppRunning = true

        super.onCreate(savedInstanceState)

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setupCrashHandler()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        App.isAppRunning = true

        super.onCreate(savedInstanceState, persistentState)

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        setupCrashHandler()
    }

    override fun onDestroy() {
        App.isAppRunning = false
        super.onDestroy()
    }

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        App.intentTransactionID = extras?.getString(TRANSACTION_ID)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        presenter?.onNewIntent(intent?.extras?.getString(TRANSACTION_ID))
    }

    override fun showWalletFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.walletFragment, null, navOptions {
            popUpTo(R.id.navigation) { inclusive = true }
            launchSingleTop = true
            anim(buildTransitionAnimation())
        })
    }

    override fun showTransactionDetailsFragment(txId: String) {
        findNavController(R.id.nav_host).navigate(R.id.transactionDetailsFragment, TransactionDetailsFragmentArgs(txId).toBundle(), navOptions {
            popUpTo(R.id.walletFragment) {}
            anim(buildTransitionAnimation())
        })
    }

    private fun buildTransitionAnimation(): AnimBuilder.() -> Unit = {
        enter = R.anim.fade_in
        popEnter = R.anim.fade_in
        exit = R.anim.fade_out
        popExit = R.anim.fade_out
    }

    fun pendingSend(info: PendingSendInfo) {
        presenter?.onPendingSend(info)
    }

    override fun startNewSnackbar(onUndo: () -> Unit, onDismiss: () -> Unit) {
        showSnackBar(getString(R.string.wallet_sent_message), onDismiss, onUndo)
    }

    override fun ensureState(): Boolean = true

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppActivityPresenter(this, AppActivityRepository())
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            RestoreManager.instance.checkDownloadStatus(id)
        }
    }

    private fun setupCrashHandler() {
        val lastException = intent.getSerializableExtra(LAST_EXCEPTION) as Throwable?

        if (lastException?.message != null) {
            showAlert(getString(R.string.crash_message), getString(R.string.i_agree), {
                Fabric.with(this, Crashlytics(), CrashlyticsNdk())

                Crashlytics.logException(lastException)

                Answers.getInstance().logCustom(CustomEvent("CRASH").
                        putCustomAttribute("message", lastException.message))
            },
                    getString(R.string.crash_title),
                    getString(R.string.crash_negative))
        }

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (e.message != null) {
                killThisProcess {
                    val intent = this.intent
                            .putExtra(RESTARTED, true)
                            .putExtra(LAST_EXCEPTION, e)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    with(this) {
                        finish()
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun killThisProcess(action: () -> Unit = {}) {
        action()

        android.os.Process.killProcess(android.os.Process.myPid())
        kotlin.system.exitProcess(10)
    }

}