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

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.eightsines.holycycle.app.ViewControllerFragment
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.screens.wallet.NavItemsAdapter
import com.mw.beam.beamwallet.screens.wallet.NavItem
import androidx.drawerlayout.widget.DrawerLayout
import com.mw.beam.beamwallet.core.views.BeamToolbar
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.navigation.NavOptions
import android.view.animation.AnimationUtils
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.ViewCompat
import com.eightsines.holycycle.ViewControllerFragmentDelegate
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.elvishew.xlog.XLog.b
import com.mw.beam.beamwallet.core.App

/**
 *  10/4/18.
 */
abstract class BaseFragment<T : BasePresenter<out MvpView, out MvpRepository>> : ViewControllerFragment(), MvpView, ScreenDelegate.ViewDelegate {
    var dialog: AlertDialog? = null

    protected var presenter: T? = null
        private set
    private val delegate = ScreenDelegate()

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navItemsAdapter: NavItemsAdapter
    private var drawerLayout: DrawerLayout? = null
    private var navigationOptions:NavOptions? = null
    private var destinationFragment:Int? = null

    private val menuItems by lazy {
        arrayOf(
                NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.wallet)),
                NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
                NavItem(NavItem.ID.UTXO, R.drawable.menu_utxo, getString(R.string.utxo)),
                NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings)))
    }

    fun showWalletFragment() {
        if ((activity as? AppActivity)?.isMenuOpened() == true) {
            (activity as? AppActivity)?.closeMenu()
        }
        else{
            (activity as? AppActivity)?.showWallet()
        }
    }


    override fun onHideKeyboard() {
    }

    override fun onShowKeyboard() {
    }

    override fun hideKeyboard() {
        delegate.hideKeyboard(activity ?: return)
    }

    override fun showKeyboard() {
        delegate.showKeyboard(activity ?: return)
    }

    override fun dismissSnackBar() {}

    override fun showSnackBar(status: Status) {
        delegate.showSnackBar(status, activity ?: return)
    }

    override fun showSnackBar(message: String, onDismiss: (() -> Unit)?, onUndo: (() -> Unit)?) {
        delegate.showSnackBar(message, activity ?: return, onDismiss, onUndo)
    }

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {
        val toolbarLayout = this.findViewById<BeamToolbar>(R.id.toolbarLayout) ?: return
        (activity as BaseActivity<*>).setupToolbar(toolbarLayout, title, hasBackArrow, hasStatus)
    }

    override fun configStatus(networkStatus: NetworkStatus) {
        (activity as BaseActivity<*>).configStatus(networkStatus)
    }

    override fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, cancelable: Boolean): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, context
                ?: return null, cancelable)
    }

    override fun showAlert(message: SpannableString, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, cancelable: Boolean): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, context
                ?: return null, cancelable)
    }

    override fun showToast(message: String, duration: Int) {
        delegate.showToast(context, message, duration)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getStatusBarColor()
    }

    open fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, android.R.color.transparent)
    }

    override fun dismissAlert() {
        delegate.dismissAlert()
    }

    override fun registerKeyboardStateListener() {
        activity?.let { delegate.registerKeyboardStateListener(it, this) }
    }

    override fun unregisterKeyboardStateListener() {
        delegate.unregisterKeyboardStateListener()
    }

    override fun vibrate(length: Long) {
        delegate.vibrate(length)
    }

    override fun addListeners() {
    }

    override fun clearListeners() {
    }

    @Suppress("UNCHECKED_CAST")
    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        presenter = initPresenter() as T
        presenter?.onCreate()
    }

    override fun onControllerContentViewCreated() {
        super.onControllerContentViewCreated()
        presenter?.onViewCreated()
    }

    override fun onControllerStart() {
        super.onControllerStart()

        if (context!=null) {
            val view = view
            if (App.isDarkMode)
            {
                view?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark_dark, context!!.theme))
            }
            else{
                view?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark, context!!.theme))
            }
        }

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

    override fun copyToClipboard(content: String?, tag: String) {
        context?.let { delegate.copyToClipboard(it, content, tag) }
    }

    override fun shareText(title: String, text: String) {
        delegate.shareText(context, title, text)
    }

    override fun openExternalLink(link: String) {
        delegate.openExternalLink(context, link)
    }

    override fun logOut() {
        (activity as BaseActivity<*>).logOut()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim == R.anim.slide_in_right) {
            val nextAnimation = AnimationUtils.loadAnimation(context, nextAnim)
            nextAnimation.setAnimationListener(object : Animation.AnimationListener {
                private var startZ = 0f
                override fun onAnimationStart(animation: Animation) {
                    view?.apply {
                        startZ = ViewCompat.getTranslationZ(this)
                        ViewCompat.setTranslationZ(this, 1f)
                    }
                }

                override fun onAnimationEnd(animation: Animation) {
                    view?.apply {
                        this.postDelayed({ ViewCompat.setTranslationZ(this, startZ) }, 100)
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return nextAnimation
        } else {
            return null
        }
    }

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val fs = this.javaClass.superclass.superclass.declaredFields
//        fs[0].isAccessible = true
//
//        val privateField = fs[0].get(this) as ViewControllerFragmentDelegate
//
//        val fs2 = privateField.javaClass.declaredFields
//        fs2[3].isAccessible = true
//        fs2[3].setInt(privateField,3)
//
//        val asyncLayoutInflater = AsyncLayoutInflater(context!!)
//        asyncLayoutInflater.inflate(onControllerGetContentLayoutId(), container) { view, _, parent ->
//            parent?.addView(view)
//
//            val fs2 = privateField.javaClass.declaredFields
//            fs2[3].isAccessible = true
//            fs2[3].setInt(privateField,3)
//
//            fs2[0].isAccessible = true
//            fs2[0].set(privateField,view)
//
//            onViewCreated(view, savedInstanceState)
//            onControllerContentViewCreated()
//
//            presenter?.onStart()
//        }
//        return null
//    }
}
