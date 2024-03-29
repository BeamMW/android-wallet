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
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.AnimBuilder
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.screens.transaction_details.TransactionDetailsFragmentArgs
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
import com.mw.beam.beamwallet.core.helpers.LockScreenManager
import io.reactivex.disposables.Disposable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.elvishew.xlog.XLog.json
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonElement
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.*
import com.mw.beam.beamwallet.core.entities.NotificationItem
import com.mw.beam.beamwallet.core.entities.NotificationType
import com.mw.beam.beamwallet.core.helpers.ScreenHelper
import com.mw.beam.beamwallet.core.views.NotificationBanner
import com.mw.beam.beamwallet.screens.address_details.AddressFragment
import com.mw.beam.beamwallet.screens.address_details.AddressFragmentArgs
import com.mw.beam.beamwallet.screens.apps.detail.AppDetailFragmentArgs
import com.mw.beam.beamwallet.screens.notifications.NotificationsFragment
import com.mw.beam.beamwallet.screens.notifications.newversion.NewVersionFragment
import com.mw.beam.beamwallet.screens.notifications.newversion.NewVersionFragmentArgs
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import com.mw.beam.beamwallet.screens.transaction_details.TransactionDetailsFragment
import com.mw.beam.beamwallet.screens.withdrawGame.WithdrawGameFragment
import kotlinx.android.synthetic.main.activity_app.*
import java.util.*
import kotlin.concurrent.schedule


class AppActivity : BaseActivity<AppActivityPresenter>(), AppActivityContract.View {

    companion object {
        lateinit var self: AppActivity

        var screenWidth = 0
        var screenHeight = 0

        var withdrawAmount = 0
        var withdrawUserId = ""

        const val IMPORT_FILE_REQUEST = 1024
        const val SHARE_CODE_REQUEST = 1025

        const val TRANSACTION_ID = "TRANSACTION_ID"
        private const val RESTARTED = "appExceptionHandler_restarted"
        private const val LAST_EXCEPTION = "appExceptionHandler_lastException"

        const val NOTIFICATION_ID_ADDRESSES = "NOTIFICATION_ID_ADDRESSES"
        const val NOTIFICATION_ID_ALL = "NOTIFICATION_ID_ALL"

        const val BUY_ID = "android.intent.action.BUY_BEAM"
        const val RECEIVE_ID = "android.intent.action.RECEIVE_BEAM"
        const val SEND_ID = "android.intent.action.SEND_BEAM"
        const val SCAN_ID = "android.intent.action.SCAN_QR"
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_app

    override fun getToolbarTitle(): String? = null

    private var result: Drawer? = null
    private lateinit var navigationView:android.view.View
    private lateinit var navItemsAdapter: NavItemsAdapter

    private lateinit var reinitNotification: Disposable
    private lateinit var lockNotification: Disposable

    private var shortCut: String? = null

    private var menuItems = mutableListOf<NavItem>()

    override fun showOpenFragment() {
        val navController = findNavController(R.id.nav_host)
        navController.navigate(R.id.welcomeOpenFragment, null, navOptions {
            popUpTo(R.id.navigation) { inclusive = true }
            launchSingleTop = true
            anim(buildTransitionAnimation())
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val density = resources.displayMetrics.density

        screenWidth = ((width - 0.5f) / density).toInt()
        screenHeight = ((height - 0.5f) / density).toInt()


        setDefaultTheme()

        App.isAppRunning = true
        App.isDarkMode =  PreferencesManager.getBoolean(PreferencesManager.DARK_MODE,false)

        changeTheme()

        super.onCreate(savedInstanceState)

        setupMenu(savedInstanceState)

      //  Fabric.with(this, Crashlytics(), CrashlyticsNdk())

       // setupCrashHandler()
        subscribeToUpdates()

        shortCut = intent.action;

        checkShortCut()

        self = this
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        setDefaultTheme()
        App.isAppRunning = true
        App.isDarkMode =  PreferencesManager.getBoolean(PreferencesManager.DARK_MODE,false)
        changeTheme()

        super.onCreate(savedInstanceState, persistentState)

        setupMenu(savedInstanceState)
      //  setupCrashHandler()
       // Fabric.with(this, Crashlytics(), CrashlyticsNdk())

        subscribeToUpdates()

        shortCut = intent.action;

        checkShortCut()
    }

    private fun setDefaultTheme()
    {
        val isDarkModeSet = PreferencesManager.getBoolean(PreferencesManager.DARK_MODE_DEFAULT,false)
        if (!isDarkModeSet) {
            val config = resources.configuration
            when (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    PreferencesManager.putBoolean(PreferencesManager.DARK_MODE_DEFAULT, true)
                    PreferencesManager.putBoolean(PreferencesManager.DARK_MODE, false)
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    PreferencesManager.putBoolean(PreferencesManager.DARK_MODE_DEFAULT, true)
                    PreferencesManager.putBoolean(PreferencesManager.DARK_MODE, true)
                }
            }
        }
    }

    fun changeTheme()
    {
        if (App.isDarkMode)
            setTheme(R.style.AppThemeDark)
        else
            setTheme(R.style.AppTheme)

        if (result!=null)
        {
            setupMenu(null)
        }
    }

    fun checkShortCut() {
        if(App.isAuthenticated && withdrawAmount > 0)
        {
            findNavController(R.id.nav_host).navigate(R.id.withdrawGameFragment, null, navOptions {
                popUpTo(R.id.walletFragment) {}
                anim(buildTransitionAnimation())
            })
        }
        else if(App.isAuthenticated && shortCut!=null)
        {
            if(shortCut == BUY_ID) {
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
            else {
                showWallet()

                when (shortCut) {
                    SEND_ID -> {
                        findNavController(R.id.nav_host).navigate(R.id.sendFragment, null, null)
                    }
                    RECEIVE_ID -> {
                        findNavController(R.id.nav_host).navigate(R.id.receiveFragment, null, null)
                    }
                    SCAN_ID -> {
                        App.isNeedOpenScanner = true;
                        findNavController(R.id.nav_host).navigate(R.id.sendFragment, null, null)
                    }
                }
            }

            shortCut = null
        }
    }

    override fun onStart() {
        super.onStart()

        DAOManager.loadApps(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMPORT_FILE_REQUEST && resultCode == RESULT_OK) {
            val selectedFile = data?.data //The uri with the location of the file

            if (selectedFile != null) {
                val text = readText(selectedFile)
                if (text.isNullOrEmpty()) {
                    showAlert(getString(R.string.incorrect_file_text), getString(R.string.ok), {

                    }, getString(R.string.incorrect_file_title))
                } else {
                    showAlert(message = getString(R.string.import_data_text),
                            btnConfirmText = getString(R.string.imp),
                            onConfirm = {
                                AppManager.instance.importData(text)
                            },
                            title = getString(R.string.import_wallet_data),
                            btnCancelText = getString(R.string.cancel))
                }
            } else {
                showAlert(getString(R.string.incorrect_file_text), getString(R.string.ok), {

                }, getString(R.string.incorrect_file_title))
            }
        } else if (requestCode == SHARE_CODE_REQUEST && resultCode == RESULT_OK) {
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                    val base = fragment as BaseFragment<*>
                    base.findNavController().popBackStack()
                }
            }
        }
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

    override fun startNewSnackbar(assetId:Int, onUndo: () -> Unit, onDismiss: () -> Unit) {
        var asset = AssetManager.instance.getAsset(assetId)?.unitName ?: ""
        if(asset.length > 8) {
            asset = asset.substring(0,8) + "..."
        }
        showSnackBar(getString(R.string.wallet_asset_sent_message, asset), onDismiss, onUndo)
    }

    override fun ensureState(): Boolean = true

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppActivityPresenter(this, AppActivityRepository())
    }

    private fun readText(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            Gson().getAdapter(JsonElement::class.java).fromJson(json) ?: return  null

            return json
        }
        catch (e: Exception) {
            return null
        }
    }

    private fun setupMenu(savedInstanceState: Bundle?)
    {
        menuItems = mutableListOf(
            NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.wallet)),
            NavItem(NavItem.ID.DAO, R.drawable.menu_dao, getString(R.string.dAppStore)),
            NavItem(NavItem.ID.DAO_CORE, R.drawable.ic_dao, getString(R.string.beam_x_dao)),
            NavItem(NavItem.ID.DAO_VOTING, R.drawable.ic_dao_voting, getString(R.string.beam_x_dao_voting)),

            NavItem(NavItem.ID.SPACE, 0, ""),
            NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
            NavItem(NavItem.ID.NOTIFICATIONS, R.drawable.menu_notification, getString(R.string.notifications)),
            NavItem(NavItem.ID.DOCUMENTATION, R.drawable.ic_help, getString(R.string.documentation)),
            NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings))
        )

        navigationView = layoutInflater.inflate(R.layout.left_menu, null)
        val navMenu = navigationView.findViewById<RecyclerView>(R.id.navMenu)

        val space = screenHeight - (8 * 60) - 190

        navItemsAdapter = NavItemsAdapter(applicationContext, menuItems.toTypedArray(), ScreenHelper.dpToPx(this, space), object : NavItemsAdapter.OnItemClickListener {
            override fun onItemClick(navItem: NavItem) {

                if (navItem.id == NavItem.ID.SPACE) {
                    return
                }

                if (navItem.id == NavItem.ID.DOCUMENTATION) {
                    val allow = PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)

                    if (allow) {
                        openExternalLink(App.documentationUrl)
                    }
                    else{
                        showAlert(
                            getString(R.string.common_external_link_dialog_message),
                            getString(R.string.open),
                            { openExternalLink(App.documentationUrl) },
                            getString(R.string.common_external_link_dialog_title),
                            getString(R.string.cancel)
                        )
                    }

                    result?.drawerLayout?.closeDrawers()
                }
                else {
                    val old = navItemsAdapter.selectedItem

                    if (navItemsAdapter.selectedItem != navItem.id) {
                        val destinationFragment = when (navItem.id) {
                            NavItem.ID.WALLET -> R.id.walletFragment
                            NavItem.ID.ADDRESS_BOOK -> R.id.addressesFragment
                            NavItem.ID.SETTINGS -> R.id.settingsFragment
                            NavItem.ID.NOTIFICATIONS -> R.id.notificationsFragment
                            NavItem.ID.DAO -> R.id.appListFragment
                            NavItem.ID.DAO_CORE -> R.id.appDetailFragment
                            NavItem.ID.DAO_VOTING -> R.id.appDetailFragment
                            else -> 0
                        }

                        if (old == NavItem.ID.NOTIFICATIONS) {
                            AppManager.instance.readAllNotification()
                            reloadNotifications();
                        }

                        val navBuilder = NavOptions.Builder()
                        val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()

                        if (navItem.id == NavItem.ID.DAO_VOTING) {
                            val args = AppDetailFragmentArgs(DAOManager.getDaoVotingApp(), true)
                            findNavController(R.id.nav_host).navigate(destinationFragment, args.toBundle(), navigationOptions);
                        }
                        else if (navItem.id == NavItem.ID.DAO_CORE) {
                            val args = AppDetailFragmentArgs(DAOManager.getDaoCoreApp(), true)
                            findNavController(R.id.nav_host).navigate(destinationFragment, args.toBundle(), navigationOptions);
                        }
                        else {
                            findNavController(R.id.nav_host).navigate(destinationFragment, null, navigationOptions);
                        }

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
            }
        })
        navMenu.layoutManager = LinearLayoutManager(applicationContext)
        navMenu.adapter = navItemsAdapter
        navItemsAdapter.selectItem(NavItem.ID.WALLET)

        result = DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(true)
                .withCustomView(navigationView)
                .build()

        result?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun subscribeToUpdates() {
        //temp fix
        reinitNotification = App.self.subOnStatusResume.subscribe(){
            val brand = Build.BRAND.toLowerCase()
            if (App.isAuthenticated && !LockScreenManager.isShowedLockScreen && brand == "xiaomi") {
                if (navItemsAdapter.selectedItem == NavItem.ID.WALLET) {
                    if (findNavController(R.id.nav_host).currentDestination?.id == R.id.walletFragment) {
                        val navController = findNavController(R.id.nav_host)
                        navController.navigate(R.id.walletFragment, null, navOptions {
                            popUpTo(R.id.navigation) { inclusive = true }
                            launchSingleTop = true
                        })
                    }
                }
            }

            if(LockScreenManager.isShowedLockScreen) {
                showLockScreen()
            }
            else if(LockScreenManager.checkIsNeedShow()) {
                showLockScreen()
            }
        }

        lockNotification = LockScreenManager.subOnStatusLock.subscribe(){
            showLockScreen()
        }
    }

    private fun setupCrashHandler() {
        val lastException = intent.getSerializableExtra(LAST_EXCEPTION) as Throwable?

        if (lastException?.message != null) {
            showAlert(getString(R.string.crash_message), getString(R.string.crash_negative), {
                FirebaseCrashlytics.getInstance().recordException(lastException);
            },
                    getString(R.string.crash_title),
                    getString(R.string.crash_positive), {})
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

    fun selectItem(item:NavItem.ID){
        navItemsAdapter.selectItem(item)
        navItemsAdapter.notifyDataSetChanged()
    }

    fun showWallet() {
        navItemsAdapter.selectItem(NavItem.ID.WALLET)

        val navBuilder = NavOptions.Builder()
        val navigationOptions = navBuilder.setPopUpTo(R.id.walletFragment, true).build()
        findNavController(R.id.nav_host).navigate(R.id.walletFragment, null, navigationOptions);
    }

    fun reloadMenu() {
        menuItems =  mutableListOf(
            NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.wallet)),
            NavItem(NavItem.ID.DAO, R.drawable.menu_dao, getString(R.string.dAppStore)),
            NavItem(NavItem.ID.DAO_CORE, R.drawable.ic_dao, getString(R.string.beam_x_dao)),
            NavItem(NavItem.ID.DAO_VOTING, R.drawable.ic_dao_voting, getString(R.string.beam_x_dao_voting)),
            NavItem(NavItem.ID.SPACE, 0, ""),
            NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
            NavItem(NavItem.ID.NOTIFICATIONS, R.drawable.menu_notification, getString(R.string.notifications)),
            NavItem(NavItem.ID.DOCUMENTATION, R.drawable.ic_help, getString(R.string.documentation)),
            NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings))
            )

        navItemsAdapter.data = menuItems.toTypedArray()
        navItemsAdapter.selectItem(NavItem.ID.SETTINGS)
        navItemsAdapter.notifyDataSetChanged()
    }

    fun reloadNotifications() {
        runOnUiThread {
            val index = menuItems.indexOfFirst {
                it.id == NavItem.ID.NOTIFICATIONS
            }
            menuItems[index].unreadCount = AppManager.instance.getUnreadNotificationsCount()
            navItemsAdapter.notifyItemChanged(index)
            sendNotifications()
        }
    }

    private fun sendNotifications() {
        val count = AppManager.instance.getUnsentNotificationsCount()
        if (count > 1 && AppManager.instance.allUnsentIsAddresses()) {
            val title = getString(R.string.addresses_expired_notif).replace("(count)", count.toString())

            val view = self.findViewById<View>(android.R.id.content)
            val banner = NotificationBanner.make(view, self, title, null, R.drawable.ic_icon_notifictions_expired, NOTIFICATION_ID_ADDRESSES,"", NotificationType.Address) { notificationId, objectId, type ->
                openNotification(notificationId, objectId, type)
            }
            banner.show()
        }
        else if (count == 1 && AppManager.instance.allUnsentIsAddresses()) {
            val notification = AppManager.instance.getUnsentNotification()

            val title = getString(R.string.address_expired_notif)

            if(notification!=null) {
                val view = self.findViewById<View>(android.R.id.content)
                val banner = NotificationBanner.make(view, self, title, null, R.drawable.ic_icon_notifictions_expired, notification.id,notification.objId, NotificationType.Address) { notificationId, objectId, type ->
                    openNotification(notificationId, objectId, type)
                }
                banner.show()
            }
        }
        else if(count > 1) {
            val title = getString(R.string.new_notifications_title)
            val detail = getString(R.string.new_notifications_text)
            val spannableString = SpannableString(detail)
            spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, detail.length-1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val view = self.findViewById<View>(android.R.id.content)
            val banner = NotificationBanner.make(view, self, title, spannableString, R.drawable.ic_icon_beam_new, NOTIFICATION_ID_ALL,"", NotificationType.News) { notificationId, objectId, type ->
                openNotification(notificationId, objectId, type)
            }
            banner.show()
        }
        else if(count == 1) {
            val notification = AppManager.instance.getUnsentNotification()
            if(notification!=null) {
                val item = NotificationItem(notification, this)
                if(item.icon!=null) {
                    val view = self.findViewById<View>(android.R.id.content)
                    val banner = NotificationBanner.make(view, self, item) { notificationId, objectId, type ->
                        openNotification(notificationId, objectId, type)
                    }
                    banner.show()
                }
            }
        }

        AppManager.instance.sendNotifications()
    }

    fun openNotification(nId:String, pId:String, type: NotificationType) {
        if(App.isAuthenticated && !isLockedScreenShow()) {
            closeMenu()

            if (nId == NOTIFICATION_ID_ADDRESSES || nId == NOTIFICATION_ID_ALL) {
                if (!isNotificationsShow()) {
                    val destinationFragment = R.id.notificationsFragment
                    val navBuilder = NavOptions.Builder()
                    val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                    findNavController(R.id.nav_host).navigate(destinationFragment, null, navigationOptions);
                    selectItem(NavItem.ID.NOTIFICATIONS)
                }
            }
            else {
                if(type == NotificationType.Address) {
                    val address = AppManager.instance.getAddress(pId)
                    if (address != null && !isAddressScreenShow(pId)) {
                        val destinationFragment = R.id.addressFragment
                        val navBuilder = NavOptions.Builder()
                        val addressArg = AddressFragmentArgs(address)
                        val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                        findNavController(R.id.nav_host).navigate(destinationFragment, addressArg.toBundle(), navigationOptions)
                    }
                }
                else if(type == NotificationType.Transaction) {
                    val transaction = AppManager.instance.getTransaction(pId)
                    if (transaction != null && !isTransactionShow(pId)) {
                        val destinationFragment = R.id.transactionDetailsFragment
                        val navBuilder = NavOptions.Builder()
                        val transactionArg = TransactionDetailsFragmentArgs(pId)
                        val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                        findNavController(R.id.nav_host).navigate(destinationFragment, transactionArg.toBundle(), navigationOptions)
                    }
                }
                else if(type == NotificationType.Version && !isNewVersionShow()) {
                    val destinationFragment = R.id.newVersionFragment
                    val navBuilder = NavOptions.Builder()
                    val versionArg = NewVersionFragmentArgs(pId)
                    val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                    findNavController(R.id.nav_host).navigate(destinationFragment, versionArg.toBundle(), navigationOptions)
                }

                AppManager.instance.wallet?.markNotificationAsRead(nId)
            }
        }
    }

    private fun isAddressScreenShow(id: String):Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                val base = fragment as BaseFragment<*>
                if (base is AddressFragment) {
                    val addressFragment = base as AddressFragment
                    if(addressFragment.addressId == id) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun isTransactionShow(id: String):Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                val base = fragment as BaseFragment<*>
                if (base is TransactionDetailsFragment) {
                    val transactionFragment = base as TransactionDetailsFragment
                    if(transactionFragment.txId == id) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isNotificationsShow():Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                val base = fragment as BaseFragment<*>
                if (base is NotificationsFragment) {
                    return true
                }
            }
        }
        return false
    }

    private fun isNewVersionShow(): Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                val base = fragment as BaseFragment<*>
                if (base is NewVersionFragment) {
                    return true
                }
            }
        }
        return false
    }
}