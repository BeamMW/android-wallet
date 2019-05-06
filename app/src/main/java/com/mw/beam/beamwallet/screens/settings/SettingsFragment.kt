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

package com.mw.beam.beamwallet.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.helpers.LockScreenManager
import com.mw.beam.beamwallet.core.helpers.isLessMinute
import com.mw.beam.beamwallet.screens.settings.password_dialog.PasswordConfirmDialog
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*
import kotlinx.android.synthetic.main.dialog_node_address.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {
    private lateinit var presenter: SettingsPresenter
    private var dialog: AlertDialog? = null

    companion object {
        fun newInstance() = SettingsFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = SettingsFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_settings
    override fun getToolbarTitle(): String? = getString(R.string.settings_title)

    override fun init(runOnRandomNode: Boolean) {
        appVersion.text = BuildConfig.VERSION_NAME
        runRandomNodeSwitch.isChecked = runOnRandomNode
        ip.text = AppConfig.NODE_ADDRESS
    }

    override fun showFingerprintSettings(isFingerprintEnabled: Boolean) {
        enableFingerprintTitle.visibility = View.VISIBLE
        enableFingerprintSwitch.visibility = View.VISIBLE
        enableFingerprintSwitch.isChecked = isFingerprintEnabled
    }

    override fun sendMailWithLogs() {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = AppConfig.SHARE_TYPE
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, AppConfig.SHARE_VALUE)
        shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))

        val uris = ArrayList<Uri>()
        val files = File(AppConfig.LOG_PATH).listFiles()

        files.asIterable().forEach {
            uris.add(FileProvider.getUriForFile(context
                    ?: return, AppConfig.AUTHORITY, File(AppConfig.LOG_PATH, it.name)))
        }

        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_send_logs_description)))
    }

    override fun changePass() = (activity as SettingsHandler).onChangePassword()

    override fun addListeners() {
        changePass.setOnClickListener {
            presenter.onChangePass()
        }

        reportProblem.setOnClickListener {
            presenter.onReportProblem()
        }

        val lockScreenSettingsOnClick = View.OnClickListener {
            presenter.onShowLockScreenSettings()
        }
        lockScreenTitle.setOnClickListener(lockScreenSettingsOnClick)
        lockScreenValue.setOnClickListener(lockScreenSettingsOnClick)

        confirmTransactionSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onChangeConfirmTransactionSettings(isChecked)
        }

        enableFingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onChangeFingerprintSettings(isChecked)
        }

        runRandomNodeSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.onChangeRunOnRandomNode(isChecked)
        }

        ip.setOnClickListener { presenter.onNodeAddressPressed() }
        ipTitle.setOnClickListener { presenter.onNodeAddressPressed() }
    }

    @SuppressLint("InflateParams")
    override fun showLockScreenSettingsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_lock_screen_settings, null)

            val time = LockScreenManager.getCurrentValue()
            val valuesArray = resources.getIntArray(R.array.lock_screen_values)

            valuesArray.forEach { millisInt ->
                val value = millisInt.toLong()

                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = getLockScreenStringValue(value)
                    isChecked = value == time
                    setOnClickListener { presenter.onChangeLockSettings(value) }
                }

                view.radioGroupLockSettings.addView(button)
            }

            view.btnCancel.setOnClickListener { presenter.onDialogClosePressed() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @SuppressLint("InflateParams")
    override fun showNodeAddressDialog(nodeAddress: String?) {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_node_address, null)

            view.nodeBtnConfirm.setOnClickListener {
                presenter.onSaveNodeAddress(view.dialogNodeValue.text.toString())
            }

            view.nodeBtnCancel.setOnClickListener { presenter.onDialogClosePressed() }

            view.dialogNodeValue.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    presenter.onChangeNodeAddress()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            if (!nodeAddress.isNullOrBlank()) {
                view.dialogNodeValue.setText(nodeAddress)
            }

            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun showInvalidNodeAddressError() {
        val textView = dialog?.findViewById<TextView>(R.id.nodeError)
        textView?.let { it.visibility = View.VISIBLE }
    }

    override fun clearInvalidNodeAddressError() {
        val textView = dialog?.findViewById<TextView>(R.id.nodeError)
        textView?.let { it.visibility = View.GONE }
    }

    override fun showConfirmPasswordDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
       PasswordConfirmDialog.newInstance(onConfirm, onDismiss)
               .show(activity?.supportFragmentManager, PasswordConfirmDialog.getFragmentTag())
    }

    private fun getLockScreenStringValue(millis: Long): String {
        return when {
            millis <= LockScreenManager.LOCK_SCREEN_NEVER_VALUE -> getString(R.string.settings_never)
            millis.isLessMinute() -> getString(R.string.settings_after_seconds, TimeUnit.MILLISECONDS.toSeconds(millis).toString())
            else -> getString(R.string.settings_after_minute, TimeUnit.MILLISECONDS.toMinutes(millis).toString())
        }
    }

    override fun updateLockScreenValue(millis: Long) {
        lockScreenValue.text = getLockScreenStringValue(millis)
    }

    override fun updateConfirmTransactionValue(isConfirm: Boolean) {
        confirmTransactionSwitch.isChecked = isConfirm
    }

    override fun closeDialog() {
        dialog?.let {
            it.dismiss()
            dialog = null
        }
    }

    override fun clearListeners() {
        confirmTransactionSwitch.setOnCheckedChangeListener(null)
        enableFingerprintSwitch.setOnCheckedChangeListener(null)
        changePass.setOnClickListener(null)
        reportProblem.setOnClickListener(null)
        lockScreenTitle.setOnClickListener(null)
        lockScreenValue.setOnClickListener(null)
        runRandomNodeSwitch.setOnCheckedChangeListener(null)
        ip.setOnClickListener(null)
        ipTitle.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SettingsPresenter(this, SettingsRepository())
        return presenter
    }

    interface SettingsHandler {
        fun onChangePassword()
    }
}
