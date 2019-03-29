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

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.utils.LockScreenManager
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*
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

    override fun init() {
        appVersion.text = BuildConfig.VERSION_NAME
        ip.text = AppConfig.NODE_ADDRESS
    }

    override fun sendMailWithLogs() {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = AppConfig.SHARE_TYPE
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, AppConfig.SHARE_VALUE)
        shareIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(AppConfig.SUPPORT_EMAIL))

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
            presenter.showLockScreenSettings()
        }
        lockScreenTitle.setOnClickListener(lockScreenSettingsOnClick)
        lockScreenValue.setOnClickListener(lockScreenSettingsOnClick)
    }

    override fun showLockScreenSettingsDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_lock_screen_settings, null)

            val time = LockScreenManager.getCurrentValue(it)
            val valueId = when (time) {
                LockScreenManager.LOCK_SCREEN_NEVER_VALUE -> R.id.lockNever
                TimeUnit.SECONDS.toMillis(15) -> R.id.lockAfter15sec
                TimeUnit.MINUTES.toMillis(1) -> R.id.lockAfter1m
                TimeUnit.MINUTES.toMillis(5) -> R.id.lockAfter5m
                TimeUnit.MINUTES.toMillis(10) -> R.id.lockAfter10m
                TimeUnit.MINUTES.toMillis(30) -> R.id.lockAfter30m
                else -> R.id.lockNever
            }
            view.radioGroupLockSettings.check(valueId)
            view.radioGroupLockSettings.setOnCheckedChangeListener { _, checkedId -> presenter.onChangeLockSettings(it, checkedId) }
            view.btnCancel.setOnClickListener { presenter.onDialogClosePressed() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun updateLockScreenValue(stringResId: Int) {
        lockScreenValue.setText(stringResId)
    }

    override fun closeDialog() {
        dialog?.let {
            it.dismiss()
            dialog = null
        }
    }

    override fun clearListeners() {
        changePass.setOnClickListener(null)
        reportProblem.setOnClickListener(null)
        lockScreenTitle.setOnClickListener(null)
        lockScreenValue.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SettingsPresenter(this, SettingsRepository())
        return presenter
    }

    interface SettingsHandler {
        fun onChangePassword()
    }
}
