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
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File

/**
 * Created by vain onnellinen on 1/21/19.
 */
class SettingsFragment : BaseFragment<SettingsPresenter>(), SettingsContract.View {
    private lateinit var presenter: SettingsPresenter

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

    override fun addListeners() {
        changePass.setOnClickListener {
            presenter.onChangePass()
        }

        reportProblem.setOnClickListener {
            presenter.onReportProblem()
        }
    }

    override fun clearListeners() {
        changePass.setOnClickListener(null)
        reportProblem.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = SettingsPresenter(this, SettingsRepository())
        return presenter
    }
}
