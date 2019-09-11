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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.eightsines.holycycle.app.ViewControllerAppCompatActivity
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import com.mw.beam.beamwallet.core.helpers.LockScreenManager
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.views.BeamToolbar
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.welcome_screen.welcome_open.*


/**
 *  10/1/18.
 */
abstract class BaseActivity<T : BasePresenter<out MvpView, out MvpRepository>> : ViewControllerAppCompatActivity(), MvpView, ScreenDelegate.ViewDelegate {
    protected var presenter: T? = null
        private set
    private val delegate = ScreenDelegate()

    private val lockScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            presenter?.onLockBroadcastReceived()
        }
    }

    override fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, cancelable: Boolean): AlertDialog? {
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

    open fun ensureState(): Boolean = App.wallet != null

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

        when (networkStatus) {
            NetworkStatus.ONLINE -> {
                handleStatus(true, toolbarLayout)
            }
            NetworkStatus.OFFLINE -> {
                handleStatus(false, toolbarLayout)
            }
            NetworkStatus.UPDATING -> {
                toolbarLayout.progressBar.visibility = View.VISIBLE
                toolbarLayout.statusIcon.visibility = View.INVISIBLE
                toolbarLayout.status.text = getString(R.string.updating).toLowerCase()
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

        registerReceiver(lockScreenReceiver, IntentFilter(LockScreenManager.LOCK_SCREEN_ACTION))
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
        unregisterReceiver(lockScreenReceiver)
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.ContextWrapper.wrap(newBase))
    }

    override fun showLockScreen() {
        if (App.isAuthenticated && !App.isShowedLockScreen) {
            App.isShowedLockScreen = true

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


    override fun logOut() {
        if (App.isAuthenticated) {
            App.isAuthenticated = false
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

    override fun shareText(title: String, text: String) {
        delegate.shareText(this, title, text)
    }

    override fun openExternalLink(link: String) {
        delegate.openExternalLink(this, link)
    }

    private fun handleStatus(isOnline: Boolean, toolbarLayout: BeamToolbar) {
        toolbarLayout.progressBar.visibility = View.INVISIBLE
        toolbarLayout.statusIcon.visibility = View.VISIBLE

        if (isOnline) {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_connected))
            toolbarLayout.status.text = getString(R.string.online).toLowerCase()
        } else {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_error))
            toolbarLayout.status.text = String.format(getString(R.string.common_status_error).toLowerCase(), AppConfig.NODE_ADDRESS)
        }
    }
}
