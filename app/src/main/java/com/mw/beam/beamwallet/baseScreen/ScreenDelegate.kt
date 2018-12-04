package com.mw.beam.beamwallet.baseScreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.Snackbar
import android.support.v4.app.SupportActivity
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
 * Created by vain onnellinen on 12/4/18.
 */
class ScreenDelegate {
    private var alert: AlertDialog? = null

    fun hideKeyboard(activity : SupportActivity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.findViewById<View>(android.R.id.content)?.windowToken, 0)
    }

    fun showSnackBar(status: AppConfig.Status, activity : SupportActivity) {
        showSnackBar(
                when (status) {
                    AppConfig.Status.STATUS_OK -> activity.getString(R.string.common_successful)
                    AppConfig.Status.STATUS_ERROR -> activity.getString(R.string.common_error)
                }, activity
        )
    }

    fun showSnackBar(message: String, activity : SupportActivity) {
        val snackBar = Snackbar.make(activity.findViewById(android.R.id.content) ?: return,
                message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.snack_bar_color))
        snackBar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
        snackBar.show()
    }

    @SuppressLint("InflateParams")
    fun showAlert(message: String, btnTextResId: Int, btnIconResId: Int, onClick: () -> Unit, context : Context): AlertDialog? {
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

    fun dismissAlert() {
        if (alert != null) {
            alert?.dismiss()
            alert = null
        }
    }
}
