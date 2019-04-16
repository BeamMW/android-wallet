package com.mw.beam.beamwallet.base_screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.eightsines.holycycle.app.ViewControllerDialogFragment
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status

abstract class BaseDialogFragment<T : BasePresenter<out MvpView, out MvpRepository>>: ViewControllerDialogFragment(), MvpView  {
    private lateinit var presenter: T
    private val delegate = ScreenDelegate()

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

    override fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean) {}

    override fun getToolbarTitle(): String? = null

    override fun configStatus(networkStatus: NetworkStatus) {}

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

    override fun addListeners() {}

    override fun clearListeners() {}

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
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        super.onDestroy()
    }

    override fun copyToClipboard(content: String?, tag: String) {
        context?.let {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText(tag, content)
        }
    }

    override fun logOut() {}
}