package com.mw.beam.beamwallet.baseScreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.view.Gravity
import com.eightsines.holycycle.app.ViewControllerAppCompatActivity
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.views.BeamToolbar
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BaseActivity<T : BasePresenter<out MvpView>> : ViewControllerAppCompatActivity(), MvpView {
    private lateinit var presenter: T
    private val delegate = ScreenDelegate()

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

    override fun showAlert(message: String, title: String, btnConfirmText: String, btnCancelText: String?, onConfirm: () -> Unit, onCancel: () -> Unit): AlertDialog? {
        return delegate.showAlert(message, title, btnConfirmText, btnCancelText, onConfirm, onCancel, baseContext)
    }

    override fun showSnackBar(status: AppConfig.Status) = delegate.showSnackBar(status, this)
    override fun showSnackBar(message: String) = delegate.showSnackBar(message, this)
    override fun hideKeyboard() = delegate.hideKeyboard(this)
    override fun dismissAlert() = delegate.dismissAlert()

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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
            return
        }

        super.onBackPressed()
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
