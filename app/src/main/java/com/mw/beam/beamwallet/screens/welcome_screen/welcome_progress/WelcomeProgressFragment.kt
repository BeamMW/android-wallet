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
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
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
    private lateinit var openTitleString: String
    private lateinit var restoreTitleString: String
    private lateinit var restoreDescriptionString: String
    private lateinit var downloadDescriptionString: String
    private lateinit var updateUtxoDescriptionString: String
    private lateinit var downloadTitleString: String
    private lateinit var createTitleString: String
    override var enableOnBackPress: Boolean = true

    companion object {
        private const val FULL_PROGRESS = 100
    }

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (enableOnBackPress) {
                presenter?.onBackPressed()
            }
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_progress
    override fun getToolbarTitle(): String? = ""

    override fun onControllerCreate(extras: Bundle?) {
        super.onControllerCreate(extras)
        openTitleString = getString(R.string.welcome_progress_open)
        restoreTitleString = getString(R.string.welcome_progress_restore)
        restoreDescriptionString = getString(R.string.welcome_progress_restore_description)
        downloadDescriptionString = getString(R.string.welcome_progress_download_description)
        updateUtxoDescriptionString = getString(R.string.welcome_progress_update_utxo_description)
        downloadTitleString = getString(R.string.downloading_blockchain_info)
        createTitleString = getString(R.string.welcome_progress_create)
    }

    override fun init(mode: WelcomeMode) {
        when (mode) {
            WelcomeMode.OPEN -> title.text = openTitleString
            WelcomeMode.RESTORE, WelcomeMode.RESTORE_AUTOMATIC -> {
                title.text = downloadTitleString
                btnCancel.visibility = View.VISIBLE
            }
            WelcomeMode.CREATE -> {
                title.text = createTitleString
                btnCancel.visibility = View.VISIBLE
            }
        }
    }

    override fun addListeners() {
        btnCancel.setOnClickListener {
            presenter?.onBackPressed()
        }
    }

    override fun clearListeners() {
        btnCancel.setOnClickListener(null)
    }

    override fun updateProgress(progressData: OnSyncProgressData, mode: WelcomeMode, isDownloadProgress: Boolean) {
        when (mode) {
            WelcomeMode.OPEN -> {
                configProgress(countProgress(progressData),"$updateUtxoDescriptionString ${progressData.done}/${progressData.total}")
            }
            WelcomeMode.RESTORE -> { }
            WelcomeMode.RESTORE_AUTOMATIC -> {
                if (isDownloadProgress) {
                    title.text = downloadTitleString
                    configProgress(progressData.done, "$downloadDescriptionString ${progressData.done}%")
                } else {
                    title.text = restoreTitleString
                    restoreFullDescription.visibility = View.VISIBLE
                    configProgress(countProgress(progressData), "$restoreDescriptionString ${countProgress(progressData)}%")
                }
            }
            WelcomeMode.CREATE -> { }
        }
    }

    override fun changeCancelButtonVisibility(visible: Boolean) {
        btnCancel.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun getLifecycleOwner(): LifecycleOwner = this

    override fun showNoInternetMessage() {
        showToast(getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT)
    }

    override fun showIncorrectNodeMessage() {
        showToast(getString(R.string.error_incorrect_node), Toast.LENGTH_SHORT)
    }

    override fun showFailedRestoreAlert() {
        showAlert(message = getString(R.string.welcome_progress_restore_networ_error_description),
                btnConfirmText = getString(R.string.welcome_progress_restore_btn_try_again),
                onConfirm = { presenter?.onTryAgain() },
                title = getString(R.string.welcome_progress_restore_error_title),
                btnCancelText = getString(R.string.cancel),
                onCancel = { presenter?.onCancel() })
    }

    override fun showFailedDownloadRestoreFileAlert() {
        showAlert(message = getString(R.string.welcome_progress_restore_error_description),
                btnConfirmText = getString(R.string.welcome_progress_restore_btn_try_again),
                onConfirm = { presenter?.onTryAgain() },
                title = getString(R.string.welcome_progress_restore_error_title),
                btnCancelText = getString(R.string.cancel),
                onCancel = { presenter?.onOkToCancelRestore() },
                cancelable = false)
    }

    override fun showCancelRestoreAlert() {
        showAlert(message = getString(R.string.welcome_progress_cancel_restore_description),
                btnConfirmText = getString(R.string.ok),
                onConfirm = { presenter?.onOkToCancelRestore() },
                title = getString(R.string.welcome_progress_cancel_restore_title),
                btnCancelText = getString(R.string.cancel),
                onCancel = { presenter?.onCancelToCancelRestore() })
    }

    override fun showCancelCreateAlert() {
        presenter?.onOkToCancelRestore()
    }

    override fun navigateToCreateFragment() {
        findNavController().navigate(WelcomeProgressFragmentDirections.actionWelcomeProgressFragmentToWelcomeCreateFragment())
    }

    private fun countProgress(progressData: OnSyncProgressData): Int {
        return (progressData.done * 100.0 / progressData.total).toInt()
    }

    //TODO decide what should be by default (arguments == null), when all modes will be available
    override fun getMode(): WelcomeMode? = arguments?.let {
        val modeName = WelcomeProgressFragmentArgs.fromBundle(it).mode
        WelcomeMode.valueOf(modeName)
    }

    override fun getPassword(): String? = arguments?.let { WelcomeProgressFragmentArgs.fromBundle(it).pass }
    override fun getSeed(): Array<String>? = arguments?.let { WelcomeProgressFragmentArgs.fromBundle(it).seed }

    override fun showWallet() {
        findNavController().navigate(WelcomeProgressFragmentDirections.actionWelcomeProgressFragmentToWalletFragment())
    }

    private fun configProgress(currentProgress: Int, descriptionString: String) {
        description.text = descriptionString
        description.visibility = View.VISIBLE
        progress.progress = currentProgress
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WelcomeProgressPresenter(this, WelcomeProgressRepository(), WelcomeProgressState())
    }
}
