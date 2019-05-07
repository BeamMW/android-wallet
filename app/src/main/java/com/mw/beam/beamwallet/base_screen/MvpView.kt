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

import android.support.v7.app.AlertDialog
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.Status

/**
 * Created by vain onnellinen on 10/1/18.
 */
interface MvpView {
    fun showKeyboard()
    fun hideKeyboard()
    fun showSnackBar(status: Status)
    fun showSnackBar(message: String)
    fun showSnackBar(message: String, textColor: Int)
    fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit = {}, title: String? = null, btnCancelText: String? = null, onCancel: () -> Unit = {}): AlertDialog?
    fun dismissAlert()
    fun copyToClipboard(content: String?, tag: String)
    fun showToast(message: String, duration: Int)
    fun initPresenter(): BasePresenter<out MvpView, out MvpRepository>
    fun initToolbar(title: String?, hasBackArrow: Boolean?, hasStatus: Boolean)
    fun configStatus(networkStatus: NetworkStatus)
    fun getToolbarTitle(): String?
    fun shareText(title: String, text: String)
    fun addListeners()
    fun clearListeners()
    fun onHideKeyboard()
    fun onShowKeyboard()
    fun registerKeyboardStateListener()
    fun unregisterKeyboardStateListener()
    fun vibrate(length: Long)
    fun logOut()
}
