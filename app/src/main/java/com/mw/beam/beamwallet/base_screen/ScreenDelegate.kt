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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
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
import android.widget.Toast
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.Status
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar
import android.os.VibrationEffect
import android.os.Build
import android.os.Vibrator
import com.mw.beam.beamwallet.core.App


/**
 * Created by vain onnellinen on 12/4/18.
 */
class ScreenDelegate {
    private var alert: AlertDialog? = null
    private var eventListener: Unregistrar? = null

    fun hideKeyboard(activity: SupportActivity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.findViewById<View>(android.R.id.content)?.windowToken, 0)
    }

    fun showKeyboard(activity: SupportActivity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    fun showSnackBar(status: Status, activity: SupportActivity) {
        showSnackBar(
                when (status) {
                    Status.STATUS_OK -> activity.getString(R.string.common_successful)
                    Status.STATUS_ERROR -> activity.getString(R.string.common_error)
                }, activity
        )
    }

    fun showSnackBar(message: String, activity: SupportActivity) {
        showSnackBar(message, R.color.colorAccent, activity)
    }

    fun showSnackBar(message: String, textColor: Int, activity: SupportActivity) {
        val snackBar = Snackbar.make(activity.findViewById(android.R.id.content) ?: return,
                message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.snack_bar_color))
        snackBar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text).setTextColor(ContextCompat.getColor(activity, textColor))
        snackBar.show()
    }

    @SuppressLint("InflateParams")
    fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, context: Context): AlertDialog? {
        val view = LayoutInflater.from(context).inflate(R.layout.common_alert_dialog, null)
        val alertTitle = view.findViewById<TextView>(R.id.title)
        val alertText = view.findViewById<TextView>(R.id.alertText)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        if (title.isNullOrBlank()) {
            alertTitle.visibility = View.GONE
        } else {
            alertTitle.text = title
        }

        alertText.text = message
        btnConfirm.text = btnConfirmText
        btnCancel.text = btnCancelText

        btnConfirm.setOnClickListener {
            onConfirm.invoke()
            alert?.dismiss()
        }
        btnCancel.setOnClickListener {
            onCancel.invoke()
            alert?.dismiss()
        }

        val dialog = AlertDialog.Builder(context).setView(view).show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert = dialog

        return alert
    }

    fun showToast(context: Context?, message: String, duration: Int) {
        Toast.makeText(context?.applicationContext, message, duration).show()
    }

    fun copyToClipboard(context: Context, message: String?, tag: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(tag, message)
    }

    fun dismissAlert() {
        if (alert != null) {
            alert?.dismiss()
            alert = null
        }
    }

    fun registerKeyboardStateListener(activity: Activity, view: ViewDelegate) {
        eventListener = KeyboardVisibilityEvent.registerEventListener(activity) {
            if (it) {
                view.onShowKeyboard()
            } else {
                view.onHideKeyboard()
            }
        }
    }

    fun unregisterKeyboardStateListener() {
        eventListener?.unregister()
    }

    fun vibrate(length: Long) {
        val vibrator = App.self.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(length, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(length)
        }
    }

    interface ViewDelegate {
        fun onHideKeyboard()
        fun onShowKeyboard()
    }
}
