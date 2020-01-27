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
package com.mw.beam.beamwallet.base_screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.eightsines.holycycle.app.ViewControllerAppCompatActivity
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.views.BeamToolbar
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import java.util.*
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_open.WelcomeOpenFragment
import android.content.res.Configuration
import com.mw.beam.beamwallet.core.helpers.*
import androidx.core.content.ContextCompat.startActivity
import android.content.ComponentName
import android.content.pm.PackageManager


/**
 *  10/1/18.
 */
abstract class BaseActivity<T : BasePresenter<out MvpView, out MvpRepository>> : ViewControllerAppCompatActivity(), MvpView, ScreenDelegate.ViewDelegate {
    protected var presenter: T? = null
        private set
    private val delegate = ScreenDelegate()

    override fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, cancelable: Boolean): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, this, cancelable)
    }

    override fun showAlert(message: SpannableString, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, cancelable: Boolean): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, this, cancelable)
    }

    override fun showSnackBar(status: Status) = delegate.showSnackBar(status, this)
    override fun showSnackBar(message: String, onDismiss: (() -> Unit)?, onUndo: (() -> Unit)?) = delegate.showSnackBar(message, this, onDismiss, onUndo)
    override fun showKeyboard() = delegate.showKeyboard(this)
    override fun hideKeyboard() = delegate.hideKeyboard(this)
    override fun dismissAlert() = delegate.dismissAlert()
    override fun showToast(message: String, duration: Int) = delegate.showToast(this, message, duration)

    override fun dismissSnackBar() {
        delegate.dismissSnackBar(this)
    }

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {
        val toolbarLayout = this.findViewById<BeamToolbar>(R.id.toolbarLayout) ?: return
        setupToolbar(toolbarLayout, title, hasBackArrow, hasStatus)
    }

    fun setupToolbar(toolbar: BeamToolbar?,title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {
        if (toolbar == null) {
            return
        }

        setSupportActionBar(toolbar.toolbar)


        if (toolbar.centerTitle) {
            supportActionBar?.title = ""
            toolbar.centerTitleView.text = title?.toUpperCase()
        } else {
            supportActionBar?.title = title
        }

        toolbar.hasStatus = hasStatus

        if (hasBackArrow != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(hasBackArrow)

            if (hasBackArrow) {
                toolbar.toolbar.setNavigationOnClickListener {
                    onBackPressed()
                }
            }
        }
    }

    open fun ensureState(): Boolean = AppManager.instance.wallet != null

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            presenter?.onClose()
            finish()
            return
        }

        super.onBackPressed()
    }

    override fun addListeners() {
    }

    override fun clearListeners() {
    }

    override fun onHideKeyboard() {
    }

    override fun onShowKeyboard() {
    }

    override fun configStatus(networkStatus: NetworkStatus) {
        val toolbarLayout = this.findViewById<BeamToolbar>(R.id.toolbarLayout) ?: return

        if (AppManager.instance.isConnecting) {
            toolbarLayout.progressBar.indeterminateDrawable.setColorFilter(getColor(R.color.category_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
            toolbarLayout.status.setTextColor(getColor(R.color.category_orange))

            toolbarLayout.progressBar.visibility = View.VISIBLE
            toolbarLayout.statusIcon.visibility = View.INVISIBLE
            toolbarLayout.status.text = getString(R.string.connecting).toLowerCase()
        }
        else{
            when (networkStatus) {
                NetworkStatus.ONLINE -> {
                    handleStatus(true, toolbarLayout)
                }
                NetworkStatus.OFFLINE -> {
                    handleStatus(false, toolbarLayout)
                }
                NetworkStatus.UPDATING -> {
                    toolbarLayout.status.setTextColor(getColor(R.color.colorAccent))
                    toolbarLayout.progressBar.indeterminateDrawable.setColorFilter(getColor(R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                    toolbarLayout.progressBar.visibility = View.VISIBLE
                    toolbarLayout.statusIcon.visibility = View.INVISIBLE
                    toolbarLayout.status.text = getString(R.string.updating).toLowerCase()
                }
            }
        }
    }

    override fun vibrate(length: Long) {
        delegate.vibrate(length)
    }

    override fun registerKeyboardStateListener() {
        delegate.registerKeyboardStateListener(this, this)
    }

    override fun unregisterKeyboardStateListener() {
        delegate.unregisterKeyboardStateListener()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        presenter = initPresenter() as T

        if (!ensureState()) {
            presenter?.onStateIsNotEnsured()
        } else {
            presenter?.onCreate()
        }
    }

    override fun onControllerContentViewCreated() {
        super.onControllerContentViewCreated()
        presenter?.onViewCreated()
    }

    override fun onControllerStart() {
        super.onControllerStart()
        presenter?.onStart()
    }

    override fun onControllerResume() {
        super.onControllerResume()
        presenter?.onResume()
    }

    override fun onControllerPause() {
        presenter?.onPause()
        super.onControllerPause()
    }

    override fun onControllerStop() {
        presenter?.onStop()
        super.onControllerStop()
    }

    override fun onDestroy() {
        presenter?.onDestroy()
        presenter = null

        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.ContextWrapper.wrap(newBase))

        androidx.multidex.MultiDex.install(this);

        val config = resources.configuration

        var locale: Locale = Locale(LocaleHelper.getCurrentLanguage().languageCode)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, resources.displayMetrics)


    }

    fun showLockScreen() {
        if (App.isAuthenticated && LockScreenManager.isShowedLockScreen && !isLockedScreenShow()) {
            LockScreenManager.inactiveDate = 0L

            if ((this as? AppActivity)?.isMenuOpened() == true) {
                (this as? AppActivity)?.closeMenu()
            }
            (this as? AppActivity)?.enableLeftMenu(false)

            delegate.dismissAlert()

            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                    val base = fragment as BaseFragment<*>
                    if (base.dialog!=null)
                    {
                        base.dialog?.dismiss()
                    }
                }
            }

            val navBuilder = NavOptions.Builder()
            navBuilder.setEnterAnim(android.R.anim.fade_in)
            navBuilder.setPopEnterAnim(android.R.anim.fade_in)
            navBuilder.setExitAnim(android.R.anim.fade_out)
            navBuilder.setPopExitAnim(android.R.anim.fade_out)

            val navigationOptions = navBuilder.build()

            findNavController(R.id.nav_host).navigate(R.id.welcomeOpenFragment, null, navigationOptions)
        }
    }

    private fun isLockedScreenShow():Boolean {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                val base = fragment as BaseFragment<*>
                if (base is WelcomeOpenFragment) {
                    return true
                }
            }
        }

        return false
    }


    override fun logOut() {
        if (App.isAuthenticated) {
            App.isAuthenticated = false

            val packageManager = applicationContext.packageManager
            val intent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
           // mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            applicationContext.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
//            startActivity(Intent(this, AppActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            })
//            finish()
        }
        else{
            startActivity(Intent(this, AppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        presenter?.onUserInteraction(applicationContext)
    }

    override fun copyToClipboard(content: String?, tag: String) = delegate.copyToClipboard(this, content, tag)

    override fun shareText(title: String, text: String, activity: Activity?) {
        delegate.shareText(this, title, text, activity)
    }

    override fun openExternalLink(link: String) {
        delegate.openExternalLink(this, link)
    }

    private fun handleStatus(isOnline: Boolean, toolbarLayout: BeamToolbar) {
        toolbarLayout.progressBar.visibility = View.INVISIBLE
        toolbarLayout.statusIcon.visibility = View.VISIBLE

        if(App.isDarkMode) {
            toolbarLayout.status.setTextColor(getColor(R.color.common_text_dark_color_dark))
        }
        else{
            toolbarLayout.status.setTextColor(getColor(R.color.common_text_dark_color))
        }

        if (isOnline) {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_connected))
            toolbarLayout.status.text = getString(R.string.online).toLowerCase()
        } else {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_error))
            toolbarLayout.status.text = String.format(getString(R.string.common_status_error).toLowerCase(), AppConfig.NODE_ADDRESS)
        }
    }
}
