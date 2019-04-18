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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_progress

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import kotlinx.android.synthetic.main.fragment_welcome_progress.*

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressFragment : BaseFragment<WelcomeProgressPresenter>(), WelcomeProgressContract.View {
    private lateinit var presenter: WelcomeProgressPresenter
    private lateinit var openTitleString: String
    private lateinit var restoreTitleString: String
    private lateinit var restoreDescriptionString: String
    private lateinit var updateUtxoDescriptionString: String

    companion object {
        private const val ARG_MODE = "ARG_MODE"
        private const val ARG_PASS = "ARG_PASS"
        private const val FULL_PROGRESS = 100
        fun newInstance(mode: WelcomeMode) = WelcomeProgressFragment().apply { arguments = Bundle().apply { putString(ARG_MODE, mode.name) } }
        fun newInstance(mode: WelcomeMode, pass: String) = WelcomeProgressFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
                putString(ARG_PASS, pass)
            }
        }

        fun getFragmentTag(): String = WelcomeProgressFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_progress
    override fun getToolbarTitle(): String? = ""

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        openTitleString = getString(R.string.welcome_progress_open)
        restoreTitleString = getString(R.string.welcome_progress_restore)
        restoreDescriptionString = getString(R.string.welcome_progress_restore_description)
        updateUtxoDescriptionString = getString(R.string.welcome_progress_update_utxo_description)
    }

    override fun init(mode: WelcomeMode) {
        when (mode) {
            WelcomeMode.OPEN -> title.text = openTitleString
            WelcomeMode.RESTORE -> {
                title.text = restoreTitleString
                restoreFullDescription.visibility = View.VISIBLE
            }
            WelcomeMode.CREATE -> {
            }
        }
    }

    override fun updateProgress(progressData: OnSyncProgressData, mode: WelcomeMode, isSyncProcess: Boolean) {
        when (mode) {
            WelcomeMode.OPEN -> {
                configProgress(countProgress(progressData), String.format(updateUtxoDescriptionString, progressData.done, progressData.total))
            }
            WelcomeMode.RESTORE -> {
                if (isSyncProcess) {
                    // FULL_PROGRESS is needed to prevent UI progress rollback after node sync was finished
                    configProgress(FULL_PROGRESS, String.format(updateUtxoDescriptionString, progressData.done, progressData.total))
                } else {
                    configProgress(countProgress(progressData), String.format(restoreDescriptionString, countProgress(progressData)))
                }
            }
            WelcomeMode.CREATE -> {
            }
        }
    }

    override fun showNoInternetConnectionMessage() {
        showToast(getString(R.string.no_internet_connection), Toast.LENGTH_LONG)
    }

    override fun showFailedRestoreAlert() {
        showAlert(message = getString(R.string.welcome_progress_restore_error_description),
                btnConfirmText = getString(R.string.welcome_progress_restore_btn_try_again),
                onConfirm = { presenter.onTryAgain() },
                title = getString(R.string.welcome_progress_restore_error_title),
                btnCancelText = getString(R.string.common_cancel),
                onCancel = { presenter.onCancel() })
    }

    private fun countProgress(progressData: OnSyncProgressData): Int {
        return (progressData.done * 100.0 / progressData.total).toInt()
    }

    //TODO decide what should be by default (arguments == null), when all modes will be available
    override fun getMode(): WelcomeMode? = WelcomeMode.valueOf(arguments?.getString(ARG_MODE)
            ?: WelcomeMode.CREATE.name)

    override fun getPassword(): String? = arguments?.getString(ARG_PASS)

    override fun showWallet() = (activity as ProgressHandler).showWallet()

    private fun configProgress(currentProgress: Int, descriptionString: String) {
        description.text = descriptionString
        description.visibility = View.VISIBLE
        progress.progress = currentProgress
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WelcomeProgressPresenter(this, WelcomeProgressRepository(), WelcomeProgressState())
        return presenter
    }

    interface ProgressHandler {
        fun showWallet()
    }
}
