package com.mw.beam.beamwallet.baseScreen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.eightsines.holycycle.app.ViewControllerAppCompatActivity
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.views.BeamButton
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BaseActivity<T : BasePresenter<out MvpView>> : ViewControllerAppCompatActivity(), MvpView {
    private lateinit var presenter: T
    private var alert: AlertDialog? = null

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

    override fun showSnackBar(status: AppConfig.Status) {
        showSnackBar(
                when (status) {
                    AppConfig.Status.STATUS_OK -> getString(R.string.common_successful)
                    AppConfig.Status.STATUS_ERROR -> getString(R.string.common_error)
                }
        )
    }

    override fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content) ?: return,
                message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.snack_bar_color))
        snackBar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        snackBar.show()
    }

    @SuppressLint("InflateParams")
    override fun showAlert(message: String, btnTextResId: Int, btnIconResId: Int, onClick: () -> Unit): AlertDialog? {
        val context = baseContext
        val view = LayoutInflater.from(context).inflate(R.layout.common_alert_dialog, null)
        val alertText = view.findViewById<TextView>(R.id.alertText)
        val button = view.findViewById<BeamButton>(R.id.button)

        alertText.text = message
        button.textResId = btnTextResId
        button.iconResId = btnIconResId
        button.setOnClickListener {
            onClick.invoke()
            alert?.dismiss()
        }

        val dialog = AlertDialog.Builder(context).setView(view).show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert = dialog

        return alert
    }

    override fun hideKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content)?.windowToken, 0)
    }

    override fun dismissAlert() {
        if (alert != null) {
            alert?.dismiss()
            alert = null
        }
    }

    protected fun initToolbar(toolbar: Toolbar, title: String, isWithStatus: Boolean = false) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (isWithStatus) {
            toolbar.findViewById<TextView>(R.id.toolbarTitle)?.text = title
        } else {
            setTitle(title)
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
