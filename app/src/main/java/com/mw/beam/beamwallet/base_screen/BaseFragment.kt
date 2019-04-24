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
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import com.eightsines.holycycle.app.ViewControllerFragment
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status

/**
 * Created by vain onnellinen on 10/4/18.
 */
abstract class BaseFragment<T : BasePresenter<out MvpView, out MvpRepository>> : ViewControllerFragment(), MvpView, ScreenDelegate.ViewDelegate {
    private lateinit var presenter: T
    private val delegate = ScreenDelegate()

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

    override fun showSnackBar(status: Status) {
        delegate.showSnackBar(status, activity ?: return)
    }

    override fun showSnackBar(message: String) {
        delegate.showSnackBar(message, activity ?: return)
    }

    override fun showSnackBar(message: String, textColor: Int) {
        delegate.showSnackBar(message, textColor, activity ?: return)
    }

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {
        (activity as BaseActivity<*>).initToolbar(title, hasBackArrow, hasStatus)
    }

    override fun configStatus(networkStatus: NetworkStatus) {
        (activity as BaseActivity<*>).configStatus(networkStatus)
    }

    override fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit): AlertDialog? {
        return delegate.showAlert(message, btnConfirmText, onConfirm, title, btnCancelText, onCancel, context
                ?: return null)
    }

    override fun showToast(message: String, duration: Int) {
        delegate.showToast(context, message, duration)
    }

    override fun dismissAlert() {
        delegate.dismissAlert()
    }

    override fun addListeners() {
    }

    override fun clearListeners() {
    }

    @Suppress("UNCHECKED_CAST")
    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        presenter = initPresenter() as T
        presenter.onCreate()
    }

    override fun onControllerContentViewCreated() {
        super.onControllerContentViewCreated()
        presenter.onViewCreated()
    }

    override fun onControllerStart() {
        super.onControllerStart()
        activity?.let { delegate.registerKeyboardStateListener(it, this) }
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
        delegate.unregisterKeyboardStateListener()
        presenter.onStop()
        super.onControllerStop()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun copyToClipboard(content: String?, tag: String) {
        context?.let { delegate.copyToClipboard(it, content, tag) }
    }

    override fun logOut() {
        (activity as BaseActivity<*>).logOut()
    }
}
