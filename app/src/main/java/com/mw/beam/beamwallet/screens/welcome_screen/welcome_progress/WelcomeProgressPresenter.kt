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

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.DownloadCalculator
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.*
import io.reactivex.disposables.Disposable
import java.io.File
import com.mw.beam.beamwallet.core.RestoreManager
import org.jetbrains.anko.runOnUiThread
import java.util.*
import kotlin.concurrent.schedule

/**
 *  1/24/19.
 */
class WelcomeProgressPresenter(currentView: WelcomeProgressContract.View, currentRepository: WelcomeProgressContract.Repository, private val state: WelcomeProgressState)
    : BasePresenter<WelcomeProgressContract.View, WelcomeProgressContract.Repository>(currentView, currentRepository),
        WelcomeProgressContract.Presenter {

    var isTrustedNodeRestor = false
    var isAlertShow = false
    var isAlreadyDownloaded = false

    lateinit var file:File
    private var recoveryPresented = false
    private var isWaitingRestore = false

    private lateinit var syncProgressUpdatedSubscription: Disposable
    private lateinit var nodeProgressUpdatedSubscription: Disposable
    private lateinit var nodeConnectionFailedSubscription: Disposable
    private lateinit var nodeStoppedSubscription: Disposable
    private lateinit var connectingSubscription: Disposable
    private lateinit var reconnectingSubscription: Disposable
    private lateinit var failedToStartNodeSubscription: Disposable
    private lateinit var nodeThreadFinishedSubscription: Disposable

    private val onRecoveryLiveData = MutableLiveData<() -> Unit>()
    private var downloadSubscription: Disposable = EmptyDisposable()
        set(value) {
            if (!field.isDisposed)
                field.dispose()

            field = value
        }

    private var importRecoverySubscription: Disposable = EmptyDisposable()
        set(value) {
            if (!field.isDisposed)
                field.dispose()

            field = value
        }

    // Stabilization window: on the open path the wallet often goes from done=0 to done=total in
    // a single tick, which renders as a useless 0% -> 100% flash. Hold the percentage back for
    // half a second and then decide once: open the wallet, or commit to showing real progress.
    private val stabilizationDelayMs = 500L
    private val trivialSyncThreshold = 2

    private val stabilizationHandler = Handler(Looper.getMainLooper())
    private var isStabilizing = false
    private var pendingProgress: OnSyncProgressData? = null
    private var hasConnected = false
    private var isReconnecting = false

    private var isNodeSyncFinished = false
    private var isFailedToStartNode = false
    private var shouldCloseWallet = false
    private var isShow = false

    override fun onCreate() {
        super.onCreate()
        isTrustedNodeRestor = view?.getIsTrustedRestore() ?: false
        state.mode = view?.getMode() ?: return
        state.password = view?.getPassword() ?: return
        state.seed = view?.getSeed()

        AppManager.instance.isRestored = state.mode == WelcomeMode.RESTORE_AUTOMATIC
    }

    override fun onStart() {
        super.onStart()

        if (state.isFailedNetworkConnect && state.mode == WelcomeMode.RESTORE_AUTOMATIC) {
            view?.showFailedDownloadRestoreFileAlert()
        }
    }

    override fun onViewCreated() {
        super.onViewCreated()

        view?.init(state.mode)

        if ((state.mode == WelcomeMode.CREATE || state.mode == WelcomeMode.OPEN) && repository.wallet != null) {
            repository.wallet?.syncWithNode()
        }

        if (state.mode == WelcomeMode.OPEN) {
            startStabilizationWindow()
        }

        onRecoveryLiveData.observe(view!!.getLifecycleOwner(), Observer {
            it.invoke()
        })

        if (state.mode == WelcomeMode.RESTORE_AUTOMATIC) {
            startAutomaticRestore()
        }
    }

    private fun startAutomaticRestore() {
        isAlreadyDownloaded = false

        state.isFailedNetworkConnect = false

        file = repository.createRestoreFile()

        downloadSubscription = RestoreManager.instance.subDownloadProgress
                .subscribe({

                    if (it.done == -1) {
                        view?.close()
                    } else {
                        onRecoveryLiveData.postValue {
                            view?.updateProgress(it, state.mode, isDownloadProgress = true, isRestoreProgress = false)
                        }

                        if (it.done == it.total) {
                            isAlreadyDownloaded = true

                            startImport()
                        }
                    }


                }, {
                    state.isFailedNetworkConnect = true
                    view?.showFailedDownloadRestoreFileAlert()
                })

        DownloadCalculator.onStartDownload()
        RestoreManager.instance.startDownload(file)
    }

    private fun startImport() {
        if (!isAlertShow)
        {
            view?.dismissAlert()
            view?.changeCancelButtonVisibility(false)
            view?.enableOnBackPress = false
            importRecoverySubscription = repository.getImportRecoveryState(state.password, state.seed?.joinToString(separator = ";", postfix = ";"), file)
                    .subscribe { data ->
                        onRecoveryLiveData.postValue {
                            view?.updateProgress(data, state.mode,isDownloadProgress = false, isRestoreProgress = false)

                            val progress = data.done.toDouble() / data.total.toFloat()

                            if (data.done == data.total) {
                                if (!recoveryPresented) {
                                    recoveryPresented = true
                                    isWaitingRestore = true

                                    // Automatic restore normally shows the wallet on the
                                    // next sync-progress event. When the wallet is already
                                    // synced once recovery finishes, no such event arrives,
                                    // so complete directly instead of waiting forever.
                                    if (state.mode == WelcomeMode.RESTORE_AUTOMATIC && AppManager.instance.isSynced()) {
                                        isWaitingRestore = false
                                        showWallet()
                                    }
                                }
                            }
                            else if (progress >= 0.99 && !recoveryPresented) {
                                recoveryPresented = true
                                isWaitingRestore = true
                            }
                        }
                    }
        }

    }

    override fun onTryAgain() {
        var isDownloadProgress = false
        if (state.mode == WelcomeMode.RESTORE_AUTOMATIC && !isAlreadyDownloaded) {
            isDownloadProgress = true
        }

        view?.updateProgress(OnSyncProgressData(0, 100), state.mode, isDownloadProgress, false)
        if (state.mode != WelcomeMode.RESTORE_AUTOMATIC) {
            repository.closeWallet()
        } else {
            startAutomaticRestore()
        }
    }

    override fun onCancel() {
        cancelRestore()
    }

    override fun onOkToCancelRestore() {
        cancelRestore()
    }

    override fun onCancelToCancelRestore() {
        if (isAlreadyDownloaded && !isAlertShow) {
            startImport()
        }
    }

    override fun onBackPressed() {
        when (state.mode) {
            WelcomeMode.RESTORE, WelcomeMode.RESTORE_AUTOMATIC -> {
                view?.showCancelRestoreAlert()
            }
            WelcomeMode.OPEN -> {
                //for now do nothing
            }
            WelcomeMode.CREATE -> {
                view?.showCancelCreateAlert()
            }
        }
    }

    override fun initSubscriptions() {

        syncProgressUpdatedSubscription = repository.getSyncProgressUpdated().subscribe {
            onProgress(it)
        }

        // Already connected when the screen opens (a warm resume, say) means no connecting event
        // is coming — without this the line would sit on "Connecting to node…" for the whole sync.
        hasConnected = AppManager.instance.getNetworkStatus() != NetworkStatus.OFFLINE

        // Feeds the connecting / reconnecting phases. BasePresenter subscribes to the same two
        // subjects for the toolbar; here they decide whether the progress line says "Connecting
        // to node…" instead of a percentage that means nothing yet.
        connectingSubscription = AppManager.instance.subOnConnectingChanged.subscribe {
            if (AppManager.instance.getNetworkStatus() != NetworkStatus.OFFLINE) {
                hasConnected = true
                isReconnecting = false
            }
        }

        reconnectingSubscription = AppManager.instance.subOnOnNetworkStartReconnecting.subscribe {
            isReconnecting = true
        }

        nodeProgressUpdatedSubscription = repository.getNodeProgressUpdated().subscribe {
            if (WelcomeMode.RESTORE == state.mode) {
                if (it.total == 0) {
//                    finishNodeProgressSubscription()
                } else {
                    view?.updateProgress(it, state.mode,isDownloadProgress = false, isRestoreProgress = false)

                    if (it.done == it.total) {
                        finishNodeProgressSubscription()
                    }
                }
            }
        }

        nodeConnectionFailedSubscription = repository.getNodeConnectionFailed().subscribe {
            when (state.mode) {
                WelcomeMode.OPEN, WelcomeMode.MOBILE_CONNECT  -> {
                    showWallet(true)
                }
                WelcomeMode.RESTORE -> {
                    if (!isFailedToStartNode) {
                        when (it) {
                            NodeConnectionError.HOST_RESOLVED_ERROR -> view?.showIncorrectNodeMessage()
                            else -> view?.showNoInternetMessage()
                        }

                        repository.closeWallet()
                        view?.logOut()
                    }
                }
                else -> {}
            }
        }

        nodeStoppedSubscription = repository.getNodeStopped().subscribe {
            if (isNodeSyncFinished) {
                repository.removeNode()

                if (Status.STATUS_OK == repository.openWallet(state.password)) {
                    view?.showWallet()
                } else {
                    view?.showSnackBar(Status.STATUS_ERROR)
                    view?.logOut()
                }
            } else {
                clearWalletProgress()

                if (Status.STATUS_OK == repository.createWallet(state.password, state.seed?.joinToString(separator = ";", postfix = ";"))) {
                    view?.init(state.mode)
                } else {
                    view?.showFailedRestoreAlert()
                }
            }
        }

        failedToStartNodeSubscription = repository.getFailedNodeStart().subscribe {
            isFailedToStartNode = true
            view?.showFailedRestoreAlert()
        }

        nodeThreadFinishedSubscription = repository.getNodeThreadFinished().subscribe {
            isFailedToStartNode = false
            clearWalletProgress()

            if (shouldCloseWallet) {
                view?.logOut()
            } else {
                if (Status.STATUS_OK == repository.createWallet(state.password, state.seed?.joinToString(separator = ";", postfix = ";"))) {
                    view?.init(state.mode)
                } else {
                    view?.showFailedRestoreAlert()
                }
            }
        }

        AppManager.instance.subOnConnectingFailed = {
            when (state.mode) {
                WelcomeMode.OPEN, WelcomeMode.MOBILE_CONNECT  -> {
                    showWallet(true)
                }
                else -> {}
            }
        }

        AppManager.instance.subOnLoginSyncProgressUpdated = {
            onProgress(it)
        }

        if ((state.mode == WelcomeMode.OPEN) && repository.wallet != null) {
            repository.wallet?.getWalletStatus()
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        return arrayOf(syncProgressUpdatedSubscription, nodeConnectionFailedSubscription, nodeProgressUpdatedSubscription, nodeStoppedSubscription, connectingSubscription, reconnectingSubscription)
    }

    override fun onDestroy() {
        importRecoverySubscription.dispose()
        downloadSubscription.dispose()
        AppManager.instance.subOnConnectingFailed = null
        AppManager.instance.subOnLoginSyncProgressUpdated = null

        // A pending stabilization callback must not fire into a dead screen: showWallet()'s
        // side effects reach past this presenter (restore file, download calculator).
        stabilizationHandler.removeCallbacksAndMessages(null)

        RestoreManager.instance.stopDownload()

        super.onDestroy()
    }

    private fun startStabilizationWindow() {
        isStabilizing = true

        stabilizationHandler.postDelayed({
            isStabilizing = false

            val pending = pendingProgress

            when {
                AppManager.instance.isSynced() -> showWallet()
                pending != null && pending.total > 0 && (pending.total - pending.done) <= trivialSyncThreshold -> showWallet()
                pending != null -> onProgress(pending)
            }
        }, stabilizationDelayMs)
    }

    /**
     * Words instead of a percentage at the two ends of a sync. Null in the steady state, where
     * the percentage is what the user actually wants to see.
     */
    private fun resolveBoundaryPhase(it: OnSyncProgressData): SyncPhase? {
        if (isReconnecting) {
            return SyncPhase.RECONNECTING
        }
        if (!hasConnected) {
            return SyncPhase.CONNECTING
        }
        if (it.total > 0 && it.done == it.total && !AppManager.instance.isSynced()) {
            return SyncPhase.FINALIZING
        }
        if (it.total > 0 && (it.total - it.done) <= trivialSyncThreshold) {
            return SyncPhase.ALMOST_DONE
        }
        return null
    }

    private fun onProgress(it: OnSyncProgressData) {
        val mobile = (PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL,false))
        val isRandom = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)
        val isOwn = !mobile && !isRandom

        Log.e("UPDATE", "${it.done} ==== ${it.total}")

        if (isStabilizing) {
            pendingProgress = it
            return
        }

        // Real numbers from the node are proof enough of a connection.
        if (it.total > 0) {
            hasConnected = true
        }

        val phase = if (state.mode == WelcomeMode.OPEN || state.mode == WelcomeMode.MOBILE_CONNECT) {
            resolveBoundaryPhase(it)
        } else {
            null
        }

        if (WelcomeMode.RESTORE != state.mode && WelcomeMode.RESTORE_AUTOMATIC != state.mode) {

            if(WelcomeMode.CREATE == state.mode && mobile && it.total == 0) {

            }
            else if(WelcomeMode.CREATE == state.mode && isOwn && it.total == 0) {

            }
            else if(WelcomeMode.RESCAN == state.mode && it.total == 0) {

            }
            else if(WelcomeMode.RESCAN == state.mode && (it.total == it.done)) {
                showWallet()
            }
            else if(WelcomeMode.MOBILE_CONNECT == state.mode && it.total == 0) {
                if(isOwn) {
                    if(AppManager.instance.isSynced()) {
                        showWallet()
                    }
                    else {
                        AppManager.instance.wallet?.syncWithNode()
                    }
                }
                else {
                    AppManager.instance.wallet?.syncWithNode()
                }
            }
            else if (it.total == 0 && WelcomeMode.RESCAN != state.mode) {
                view?.updateProgress(OnSyncProgressData(1, 1), state.mode,isDownloadProgress = false, isRestoreProgress = false)
                showWallet()
            }
            else {
                view?.updateProgress(it, state.mode,isDownloadProgress = false, isRestoreProgress = false, phase = phase)

                if (it.done == it.total) {
                    showWallet()
                }
            }
        }
        else if(WelcomeMode.RESTORE_AUTOMATIC == state.mode) {
            if (isWaitingRestore && recoveryPresented) {
                isWaitingRestore = false
                showWallet()
            }
            else {
                view?.updateProgress(it, state.mode, isDownloadProgress = false, isRestoreProgress = true)
                if (it.done == it.total && isWaitingRestore && recoveryPresented) {
                    isWaitingRestore = false
                    showWallet()
                }
            }
        }
        else if (isNodeSyncFinished && it.total > 0) {
            view?.updateProgress(it, state.mode, isDownloadProgress = true, isRestoreProgress = false)

            if (it.done == it.total) {
                //sometimes lib notifies us few times about end of progress
                //so we need to unsubscribe from events to prevent unexpected behaviour
                syncProgressUpdatedSubscription.dispose()
                repository.closeWallet()
            }
        }
    }

    private fun showWallet(error:Boolean = false) {
       if(!isShow) {

           val mobile = (PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL,false))
           val isRandom = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)
           val isOwn = !mobile && !isRandom

           if (isOwn && !AppManager.instance.isSynced() && !error) {
               return
           }

           AppManager.instance.subOnConnectingFailed = null
           AppManager.instance.subOnLoginSyncProgressUpdated = null

           isShow = true

           disposable.dispose()

           repository.removeRestoreFile()

           DownloadCalculator.onStopDownload()

           view?.showWallet()
       }
    }

    private fun finishNodeProgressSubscription() {
        nodeProgressUpdatedSubscription.dispose()
        isNodeSyncFinished = true
    }

    private fun clearWalletProgress() {
        repository.removeWallet()
        repository.removeNode()
    }

    private fun cancelRestore() {
        RestoreManager.instance.stopDownload()

        shouldCloseWallet = true
        importRecoverySubscription.dispose()
        downloadSubscription.dispose()

        AppManager.instance.isRestored = false

        view?.navigateToCreateFragment()
    }

    override fun hasBackArrow(): Boolean? = false
}
