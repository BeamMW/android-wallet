package com.mw.beam.beamwallet.baseScreen

import android.support.v7.app.AlertDialog
import com.mw.beam.beamwallet.core.AppConfig

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface MvpView {
    fun hideKeyboard()
    fun showSnackBar(status: AppConfig.Status)
    fun showSnackBar(message: String)
    fun showAlert(message: String, btnTextResId: Int, btnIconResId: Int, onClick: () -> Unit = {}): AlertDialog?
}
