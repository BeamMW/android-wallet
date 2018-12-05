package com.mw.beam.beamwallet.baseScreen

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.eightsines.holycycle.app.ViewControllerFragment
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppConfig


/**
 * Created by vain onnellinen on 10/4/18.
 */
abstract class BaseFragment<T : BasePresenter<out MvpView>> : ViewControllerFragment(), MvpView {
    private lateinit var presenter: T
    private val delegate = ScreenDelegate()

    override fun hideKeyboard() {
        delegate.hideKeyboard(activity ?: return)
    }

    override fun showSnackBar(status: AppConfig.Status) {
        delegate.showSnackBar(status, activity ?: return)
    }

    override fun showSnackBar(message: String) {
        delegate.showSnackBar(message, activity ?: return)
    }

    protected fun setTitle(title: String) {
        activity?.findViewById<View>(R.id.toolbarLayout)?.findViewById<TextView>(R.id.toolbarTitle)?.text = title
    }

    override fun showAlert(message: String, title: String, btnConfirmText: String, btnCancelText: String?, onConfirm: () -> Unit, onCancel: () -> Unit): AlertDialog? {
        return delegate.showAlert(message, title, btnConfirmText, btnCancelText, onConfirm, onCancel, context
                ?: return null)
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
}
