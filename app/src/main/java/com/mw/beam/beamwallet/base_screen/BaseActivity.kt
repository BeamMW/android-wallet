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

import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import com.eightsines.holycycle.app.ViewControllerAppCompatActivity
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.utils.LockScreenManager
import com.mw.beam.beamwallet.core.views.BeamToolbar
import com.mw.beam.beamwallet.screens.welcome_screen.WelcomeActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BaseActivity<T : BasePresenter<out MvpView, out MvpRepository>> : ViewControllerAppCompatActivity(), MvpView {
    private lateinit var presenter: T
    private val delegate = ScreenDelegate()
    private val lockScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            presenter.onLockBroadcastReceived()
        }
    }

    protected fun showFragment(
            fragment: Fragment,
            tag: String,
            clearToTag: String?,
            clearInclusive: Boolean
    ) {
        drawerLayout?.closeDrawer(Gravity.START)
        val fragmentManager = supportFragmentManager
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

        if (currentFragment == null || tag != currentFragment.tag) {
            if (clearToTag != null || clearInclusive) {
                fragmentManager.popBackStack(
                        clearToTag,
                        if (clearInclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
                )
            }

            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment, tag)
            transaction.addToBackStack(tag)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.commit()
        }
    }

    override fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, this)
    }

    override fun showSnackBar(status: Status) = delegate.showSnackBar(status, this)
    override fun showSnackBar(message: String) = delegate.showSnackBar(message, this)
    override fun showSnackBar(message: String, textColor: Int) = delegate.showSnackBar(message, textColor, this)
    override fun showKeyboard() = delegate.showKeyboard(this)
    override fun hideKeyboard() = delegate.hideKeyboard(this)
    override fun dismissAlert() = delegate.dismissAlert()
    override fun showToast(message: String, duration: Int) = delegate.showToast(this, message, duration)

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {
        val toolbarLayout = this.findViewById<BeamToolbar>(R.id.toolbarLayout) ?: return
        setSupportActionBar(toolbarLayout.toolbar)
        supportActionBar?.title = title
        toolbarLayout.hasStatus = hasStatus

        if (hasBackArrow != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(hasBackArrow)

            if (hasBackArrow) {
                toolbarLayout.toolbar.setNavigationOnClickListener {
                    onBackPressed()
                }
            }
        }
    }

    open fun ensureState(): Boolean = App.wallet != null

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            presenter.onClose()
            finish()
            return
        }

        super.onBackPressed()
    }

    override fun addListeners() {
    }

    override fun clearListeners() {
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
                toolbarLayout.status.text = getString(R.string.common_status_updating)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        presenter = initPresenter() as T

        if (!ensureState()) {
            presenter.onStateIsNotEnsured()
        } else {
            presenter.onCreate()
        }

        registerReceiver(lockScreenReceiver, IntentFilter(LockScreenManager.LOCK_SCREEN_ACTION))
    }

    override fun onControllerContentViewCreated() {
        super.onControllerContentViewCreated()
        presenter.onViewCreated()
    }

    override fun onControllerStart() {
        super.onControllerStart()
        presenter.onStart()
    }

    override fun onControllerResume() {
        super.onControllerResume()
        presenter.onResume()
    }

    override fun onControllerPause() {
        presenter.onPause()
        super.onControllerPause()
    }

    override fun onControllerStop() {
        presenter.onStop()
        super.onControllerStop()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        unregisterReceiver(lockScreenReceiver)
        super.onDestroy()
    }

    override fun logOut() {
        startActivity(Intent(applicationContext, WelcomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        presenter.onUserInteraction(applicationContext)
    }

    override fun copyToClipboard(content: String?, tag: String) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(tag, content)
    }

    private fun handleStatus(isOnline: Boolean, toolbarLayout: BeamToolbar) {
        toolbarLayout.progressBar.visibility = View.INVISIBLE
        toolbarLayout.statusIcon.visibility = View.VISIBLE

        if (isOnline) {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_connected))
            toolbarLayout.status.text = getString(R.string.common_status_online)
        } else {
            toolbarLayout.statusIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.status_error))
            toolbarLayout.status.text = String.format(getString(R.string.common_status_error), AppConfig.NODE_ADDRESS)
        }
    }
}
