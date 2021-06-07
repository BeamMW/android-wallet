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

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_welcome_progress.*
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

/**
 *  1/24/19.
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

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (enableOnBackPress) {
                presenter?.onBackPressed()
            }
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_welcome_progress
    override fun getToolbarTitle(): String? = ""
    private var timer: Timer? = null
    private var isShowWallet = false

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
        val mobile = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        val isRandom = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)
        val isOwn = !mobile && !isRandom

        if(mode != WelcomeMode.MOBILE_CONNECT) {
            AppManager.instance.removeOldValues()
        }

        presenter?.repository?.setContext(requireContext())

        when (mode) {
            WelcomeMode.MOBILE_CONNECT -> {
                title.text = getString(R.string.connect_to_mobilenode)
                btnCancel.visibility = View.VISIBLE
                appVersion.visibility = View.GONE
                restoreFullDescription.visibility = View.VISIBLE
                restoreFullDescriptionText1.text = getString(R.string.please_no_lock)
                restoreFullDescriptionText2.visibility = View.GONE
                val descriptionString = getString(R.string.syncing_with_blockchain) + " " + 0 + "%"
                configProgress(0, descriptionString)
            }
            WelcomeMode.OPEN -> {
                title.text = openTitleString
                if (mobile) {
                    btnCancel.visibility = View.VISIBLE
                    restoreFullDescription.visibility = View.VISIBLE
                    restoreFullDescriptionText1.text = getString(R.string.please_no_lock)
                    restoreFullDescriptionText2.visibility = View.GONE
                    val descriptionString = getString(R.string.syncing_with_blockchain) + " " + 0 + "%"
                    configProgress(0, descriptionString)
                }
            }
            WelcomeMode.RESTORE, WelcomeMode.RESTORE_AUTOMATIC -> {
                title.text = downloadTitleString
                btnCancel.visibility = View.VISIBLE
                appVersion.visibility = View.VISIBLE
            }
            WelcomeMode.CREATE -> {
                if(mobile || isOwn) {
                    restoreFullDescription.visibility = View.VISIBLE
                    restoreFullDescriptionText1.text = getString(R.string.please_no_lock)
                    restoreFullDescriptionText2.visibility = View.GONE
                    val descriptionString = getString(R.string.syncing_with_blockchain) + " " + 0 + "%"
                    configProgress(0, descriptionString)
                }
                title.text = createTitleString
                btnCancel.visibility = View.VISIBLE
                appVersion.visibility = View.VISIBLE
            }
        }

        if(mode == WelcomeMode.OPEN || mode == WelcomeMode.CREATE) {
            timer?.cancel()
            timer = null
            if(mode == WelcomeMode.CREATE && mobile) {

            }
            else if(mode == WelcomeMode.CREATE && isOwn) {

            }
            else if(mode == WelcomeMode.OPEN && mobile) {

            }
            else {
                timer = Timer()
                timer?.schedule(timerTask {
                    if (!App.isAuthenticated) {
                        AppActivity.self.runOnUiThread {
                            showWallet()
                        }
                    }
                }, 2000)
            }
        }
    }

    override fun addListeners() {
        btnCancel.setOnClickListener {
            if(getMode() == WelcomeMode.MOBILE_CONNECT) {
                findNavController().popBackStack()
            }
            else {
                val mobile = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
                if(mobile && getMode() == WelcomeMode.OPEN) {
                    Api.closeWallet()
                    AppManager.instance.wallet = null
                    AppManager.instance.unSubscribeToUpdates()
                    logOut()
                }
                else {
                    presenter?.onBackPressed()
                }
            }
        }
    }

    override fun clearListeners() {
        btnCancel.setOnClickListener(null)
    }

    override fun updateProgress(progressData: OnSyncProgressData, mode: WelcomeMode, isDownloadProgress: Boolean, isRestoreProgress: Boolean) {
        when (mode) {
            WelcomeMode.OPEN -> {
                val mobile = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
                if (mobile) {
                    val percent = (progressData.done.toDouble() / progressData.total.toDouble()) * 100.0
                    val descriptionString = getString(R.string.syncing_with_blockchain) + " " + percent.toInt().toString() + "%"
                    configProgress(countProgress(progressData), descriptionString)
                }
                else {
                    configProgress(countProgress(progressData), "$updateUtxoDescriptionString ${progressData.done}/${progressData.total}")
                }
            }
            WelcomeMode.MOBILE_CONNECT -> {
                val percent = (progressData.done.toDouble() / progressData.total.toDouble()) * 100.0
                val descriptionString = getString(R.string.syncing_with_blockchain) + " " + percent.toInt().toString() + "%"
                configProgress(countProgress(progressData), descriptionString)
            }
            WelcomeMode.RESTORE -> {
            }
            WelcomeMode.RESTORE_AUTOMATIC -> {
                if (isDownloadProgress) {
                    var descriptionString = if (progressData.time != null) {
                        val estimate = "${getString(R.string.estimted_time).toLowerCase()} ${progressData.time.toTimeFormat(context)}."
                        "$downloadDescriptionString ${progressData.done}%. $estimate"
                    } else {
                        "$downloadDescriptionString ${progressData.done}%."
                    }

                    title.text = downloadTitleString

                    restoreFullDescription.visibility = View.GONE

                    configProgress(progressData.done, descriptionString)
                } else if (isRestoreProgress) {
                    val percent = (progressData.done.toDouble() / progressData.total.toDouble()) * 100.0

                    val descriptionString = getString(R.string.sync_with_node) + ": " + percent.toInt().toString() + "%"

                    title.text = restoreTitleString
                    restoreFullDescription.visibility = View.VISIBLE
                    configProgress(countProgress(progressData), descriptionString)
                } else {
                    var descriptionString = if (progressData.time != null) {
                        val estimate = "${getString(R.string.estimted_time).toLowerCase()} ${progressData.time.toTimeFormat(context)}."
                        "$restoreDescriptionString ${countProgress(progressData)}%. $estimate"
                    } else {
                        "$restoreDescriptionString ${countProgress(progressData)}%."
                    }

                    title.text = restoreTitleString

                    restoreFullDescription.visibility = View.VISIBLE

                    configProgress(countProgress(progressData), descriptionString)
                }
            }
            WelcomeMode.CREATE -> {
                val percent = (progressData.done.toDouble() / progressData.total.toDouble()) * 100.0
                val descriptionString = getString(R.string.syncing_with_blockchain) + " " + percent.toInt().toString() + "%"
                configProgress(countProgress(progressData), descriptionString)
            }
        }
    }

    override fun changeCancelButtonVisibility(visible: Boolean) {
        btnCancel.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)
        appVersion.text = "v " + BuildConfig.VERSION_NAME
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
       presenter?.isAlertShow = true

        showAlert(message = getString(R.string.welcome_progress_cancel_restore_description),
                btnConfirmText = getString(R.string.ok),
                onConfirm = {
                    presenter?.isAlertShow = false
                    presenter?.onOkToCancelRestore()
                },
                title = getString(R.string.welcome_progress_cancel_restore_title),
                btnCancelText = getString(R.string.cancel),
                onCancel = {
                    presenter?.isAlertShow = false
                    presenter?.onCancelToCancelRestore()
                }
        )
    }

    override fun showCancelCreateAlert() {
        presenter?.onOkToCancelRestore()
    }

    override fun navigateToCreateFragment() {
        if (isRecoverDataBaseExists()) {
            findNavController().popBackStack(R.id.welcomeOpenFragment, false)
        }
        else{
            findNavController().popBackStack(R.id.welcomeCreateFragment, false)
        }
    }

    override fun close() {
        presenter?.onOkToCancelRestore()
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
    override fun getIsTrustedRestore(): Boolean? = arguments?.let { WelcomeProgressFragmentArgs.fromBundle(it).isTrustedRestore }

    override fun showWallet() {
        if(!isShowWallet && getMode() == WelcomeMode.MOBILE_CONNECT && App.isAuthenticated) {
            isShowWallet = true

            timer?.cancel()
            timer = null

            Handler().postDelayed({
                AppActivity.self.runOnUiThread {
                    showToast(getString(R.string.wallet_connected_to_mobile_node), 4000)
                    findNavController().navigate(WelcomeProgressFragmentDirections.actionWelcomeProgressFragmentToWalletFragment())
                }
            }, 1000)

            return
        }
        else if (isShowWallet && !App.isAuthenticated ) {
            return
        }
        else if (App.isAuthenticated ) {
            return
        }


        isShowWallet = true

        timer?.cancel()
        timer = null

        val recoverFile = File(AppConfig.DB_PATH, AppConfig.DB_FILE_NAME_RECOVER)
        val journalRecoverFile = File(AppConfig.DB_PATH, AppConfig.NODE_JOURNAL_FILE_NAME_RECOVER)

        if (recoverFile.exists()) {
            PreferencesManager.putString(PreferencesManager.KEY_TAG_DATA_RECOVER, "")
            recoverFile.delete()
        }

        if (journalRecoverFile.exists()) {
            journalRecoverFile.delete()
        }


        App.isAuthenticated = true

        AppManager.instance.subscribeToUpdates()

        App.self.clearLogs()

        if(PreferencesManager.getBoolean(PreferencesManager.KEY_BACKGROUND_MODE, false)) {
            App.self.startBackgroundService()
        }

        findNavController().navigate(WelcomeProgressFragmentDirections.actionWelcomeProgressFragmentToWalletFragment())

//        val navBuilder = NavOptions.Builder()
//        navBuilder.setEnterAnim(R.anim.fade_in)
//        navBuilder.setPopEnterAnim(R.anim.fade_in)
//        navBuilder.setExitAnim(R.anim.fade_out)
//        navBuilder.setPopExitAnim(R.anim.fade_out)
//
//        clearBackStack()
//        clearAllFragments()
//        findNavController().navigate(R.id.walletFragment, null, navBuilder.build())
    }

    fun clearBackStack() {
        val fragmentManager = activity?.supportFragmentManager
        if(fragmentManager != null) {
            if (fragmentManager?.backStackEntryCount > 0) {
                val first: FragmentManager.BackStackEntry = fragmentManager.getBackStackEntryAt(0)
                fragmentManager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    fun clearAllFragments() {
        if(activity?.supportFragmentManager != null) {
            for (fragment in requireActivity().supportFragmentManager.fragments) {
                if (fragment != null) {
                    requireActivity().supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
            }
        }
    }

    private fun configProgress(currentProgress: Int, descriptionString: String) {
        if(description != null) {
            description.text = descriptionString
            description.visibility = View.VISIBLE

            if (progress.progress == 0 && currentProgress == 100) {
                ObjectAnimator.ofInt(progress, "progress", 100)
                        .setDuration(500)
                        .start()
            }
            else{
                progress.progress = currentProgress
            }
        }
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WelcomeProgressPresenter(this, WelcomeProgressRepository(), WelcomeProgressState())
    }
}
