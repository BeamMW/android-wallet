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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.eightsines.holycycle.app.ViewControllerDialogFragment
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status

abstract class BaseDialogFragment<T : BasePresenter<out MvpView, out MvpRepository>>: ViewControllerDialogFragment(), MvpView, ScreenDelegate.ViewDelegate  {

    open var isMatchParent = true

    protected var presenter: T? = null
        private set
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

    override fun showSnackBar(message: String, onDismiss: (() -> Unit)?, onUndo: (() -> Unit)?) {
        delegate.showSnackBar(message, activity ?: return, onDismiss, onUndo)
    }

    override fun dismissSnackBar() {}

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {}

    override fun getToolbarTitle(): String? = null

    override fun configStatus(networkStatus: NetworkStatus) {}

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

    override fun dismissAlert() {
        delegate.dismissAlert()
    }

    override fun vibrate(length: Long) {
        delegate.vibrate(length)
    }

    override fun registerKeyboardStateListener() {
        activity?.let { delegate.registerKeyboardStateListener(it, this) }
    }

    override fun unregisterKeyboardStateListener() {
        delegate.unregisterKeyboardStateListener()
    }

    override fun addListeners() {}

    override fun clearListeners() {}

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
        if (isMatchParent) {
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        context?.let {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText(tag, content))
        }
    }

    override fun shareText(title: String, text: String, activity: Activity?) {
        delegate.shareText(context, title, text, activity)
    }

    override fun openExternalLink(link: String) {
        delegate.openExternalLink(context, link)
    }

    override fun logOut() {}
}
