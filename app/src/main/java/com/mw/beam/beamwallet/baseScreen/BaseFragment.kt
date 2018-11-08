package com.mw.beam.beamwallet.baseScreen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.views.BeamButton


/**
 * Created by vain onnellinen on 10/4/18.
 */
abstract class BaseFragment<T : BasePresenter<out MvpView>> : Fragment(), MvpView {
    private lateinit var presenter: T
    private var alert: AlertDialog? = null

    fun configPresenter(presenter: T) {
        this.presenter = presenter
        this.presenter.viewIsReady()
    }

    override fun hideKeyboard() {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity?.findViewById<View>(android.R.id.content)?.windowToken, 0)
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
        val context = context ?: return
        val snackBar = Snackbar.make(activity?.findViewById(android.R.id.content) ?: return,
                message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.snack_bar_color))
        snackBar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        snackBar.show()
    }

    protected fun setTitle(title: String) {
        activity?.findViewById<View>(R.id.toolbarLayout)?.findViewById<TextView>(R.id.toolbarTitle)?.text = title
    }

    @SuppressLint("InflateParams")
    override fun showAlert(message: String, btnTextResId: Int, btnIconResId: Int, onClick: () -> Unit): AlertDialog? {
        val context = context ?: return null
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

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun onStop() {
        if (alert != null) {
            alert?.dismiss()
            alert = null
        }

        presenter.detachView()
        presenter.onStop()

        super.onStop()
    }
}
