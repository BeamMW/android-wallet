package com.mw.beam.beamwallet.base_screen

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status

abstract class BaseBottomSheetDialogFragment<T : BasePresenter<out MvpView, out MvpRepository>>: BottomSheetDialogFragment(), MvpView, ScreenDelegate.ViewDelegate  {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter() as T
        presenter?.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter?.onViewCreated()
    }

    override fun onStart() {
        super.onStart()
        presenter?.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }


    override fun onPause() {
        presenter?.onPause()
        super.onPause()
    }

    override fun onStop() {
        presenter?.onStop()
        super.onStop()
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