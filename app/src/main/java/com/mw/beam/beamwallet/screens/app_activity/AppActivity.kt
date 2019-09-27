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
import android.content.res.Configuration
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
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.screens.wallet.NavItem
import com.mw.beam.beamwallet.screens.wallet.NavItemsAdapter
import android.os.Handler
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import android.widget.TextView
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.AppConfig


class AppActivity : BaseActivity<AppActivityPresenter>(), AppActivityContract.View {

    companion object {
        const val TRANSACTION_ID = "TRANSACTION_ID"
        private const val RESTARTED = "appExceptionHandler_restarted"
        private const val LAST_EXCEPTION = "appExceptionHandler_lastException"
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_app

    override fun getToolbarTitle(): String? = null

    private var result: Drawer? = null
    private lateinit var navigationView:android.view.View

    private lateinit var navItemsAdapter: NavItemsAdapter

    private val menuItems by lazy {
        arrayOf(
                NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.wallet)),
                NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
                NavItem(NavItem.ID.UTXO, R.drawable.menu_utxo, getString(R.string.utxo)),
                NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings)))
    }

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

        setupMenu(savedInstanceState)
        setupCrashHandler()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        App.isAppRunning = true

        super.onCreate(savedInstanceState, persistentState)

        setupMenu(savedInstanceState)
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


    private fun setupMenu(savedInstanceState: Bundle?)
    {
        navigationView = layoutInflater.inflate(R.layout.left_menu, null)
        val navMenu = navigationView.findViewById<RecyclerView>(R.id.navMenu)

        navItemsAdapter = NavItemsAdapter(applicationContext, menuItems, object : NavItemsAdapter.OnItemClickListener {
            override fun onItemClick(navItem: NavItem) {
                if (navItemsAdapter.selectedItem != navItem.id) {
                    val destinationFragment = when (navItem.id) {
                        NavItem.ID.WALLET -> R.id.walletFragment
                        NavItem.ID.ADDRESS_BOOK -> R.id.addressesFragment
                        NavItem.ID.UTXO -> R.id.utxoFragment
                        NavItem.ID.SETTINGS -> R.id.settingsFragment
                        else -> 0
                    }
                    val navBuilder = NavOptions.Builder()
                    val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                    findNavController(R.id.nav_host).navigate(destinationFragment, null, navigationOptions);

                    val mDelayOnDrawerClose = 50
                    Handler().postDelayed({
                        result?.drawerLayout?.closeDrawers()
                        navItemsAdapter.selectItem(navItem.id)
                    }, mDelayOnDrawerClose.toLong())
                }
                else{
                    result?.drawerLayout?.closeDrawers()
                }
            }
        })
        navMenu.layoutManager = LinearLayoutManager(applicationContext)
        navMenu.adapter = navItemsAdapter
        navItemsAdapter.selectItem(NavItem.ID.WALLET)

        val whereBuyBeamLink = navigationView.findViewById<TextView>(R.id.whereBuyBeamLink)
        whereBuyBeamLink.setOnClickListener {

            val allow = PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)

            if (allow) {
                openExternalLink(AppConfig.BEAM_EXCHANGES_LINK)
            }
            else{
                showAlert(
                        getString(R.string.common_external_link_dialog_message),
                        getString(R.string.open),
                        { openExternalLink(AppConfig.BEAM_EXCHANGES_LINK) },
                        getString(R.string.common_external_link_dialog_title),
                        getString(R.string.cancel)
                )
            }

        }

        result = DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(true)
                .withCustomView(navigationView)
                .build()

        result?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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

    fun enableLeftMenu(enable:Boolean) {
        if (enable) {
            result?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
        else{
            result?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun isMenuOpened() : Boolean {
       return result?.isDrawerOpen == true
    }

    fun closeMenu() {
        result?.closeDrawer()
    }

    fun openMenu() {
        result?.openDrawer()
    }

    fun showWallet() {
        navItemsAdapter.selectItem(NavItem.ID.WALLET)

        val navBuilder = NavOptions.Builder()
        val navigationOptions = navBuilder.setPopUpTo(R.id.walletFragment, true).build()
        findNavController(R.id.nav_host).navigate(R.id.walletFragment, null, navigationOptions);
    }

    fun reloadMenu() {
        val whereBuyBeamLink = navigationView.findViewById<TextView>(R.id.whereBuyBeamLink)
        whereBuyBeamLink.text = getString(R.string.welcome_where_to_buy_beam)

        navItemsAdapter.data = arrayOf(
                NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.wallet)),
                NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
                NavItem(NavItem.ID.UTXO, R.drawable.menu_utxo, getString(R.string.utxo)),
                NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings)))
        navItemsAdapter.selectItem(NavItem.ID.SETTINGS)
        navItemsAdapter.notifyDataSetChanged()
    }
}