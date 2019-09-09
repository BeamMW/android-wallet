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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.File

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressPresenter(currentView: WelcomeProgressContract.View, currentRepository: WelcomeProgressContract.Repository, private val state: WelcomeProgressState)
    : BasePresenter<WelcomeProgressContract.View, WelcomeProgressContract.Repository>(currentView, currentRepository),
        WelcomeProgressContract.Presenter {

    var isTrustedNodeRestor = false
    var isAlertShow = false
    var isAlreadyDownloaded = false

    lateinit var file:File

    private lateinit var syncProgressUpdatedSubscription: Disposable
    private lateinit var nodeProgressUpdatedSubscription: Disposable
    private lateinit var nodeConnectionFailedSubscription: Disposable
    private lateinit var nodeStoppedSubscription: Disposable
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

    private var isNodeSyncFinished = false
    private var isFailedToStartNode = false
    private var shouldCloseWallet = false

    override fun onCreate() {
        super.onCreate()
        isTrustedNodeRestor = view?.getIsTrustedRestore() ?: false
        state.mode = view?.getMode() ?: return
        state.password = view?.getPassword() ?: return
        state.seed = view?.getSeed()
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

        downloadSubscription = repository.downloadRestoreFile(file)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    onRecoveryLiveData.postValue {
                        view?.updateProgress(it, state.mode, true)
                    }

                    if (it.done == it.total) {
                        isAlreadyDownloaded = true
                        startImport()
                    }
                }, {
                    state.isFailedNetworkConnect = true
                    view?.showFailedDownloadRestoreFileAlert()
                })
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
                            view?.updateProgress(data, state.mode)

                            if (data.done == data.total) {
                                showWallet()
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

        view?.updateProgress(OnSyncProgressData(0, 100), state.mode, isDownloadProgress)
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
            if (WelcomeMode.RESTORE != state.mode && WelcomeMode.RESTORE_AUTOMATIC != state.mode) {
                if (it.total == 0 || isTrustedNodeRestor ||
                        (PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE,false)
                                && PreferencesManager.getBoolean(PreferencesManager.KEY_RESTORED_FROM_TRUSTED,false))) {
                    view?.updateProgress(OnSyncProgressData(1, 1), state.mode)
                    showWallet()
                }
                else {
                    view?.updateProgress(it, state.mode)

                    if (it.done == it.total) {
                        showWallet()
                    }
                }
            } else if (isNodeSyncFinished && it.total > 0) {
                view?.updateProgress(it, state.mode, true)

                if (it.done == it.total) {
                    //sometimes lib notifies us few times about end of progress
                    //so we need to unsubscribe from events to prevent unexpected behaviour
                    syncProgressUpdatedSubscription.dispose()
                    repository.closeWallet()
                }
            }
        }

        nodeProgressUpdatedSubscription = repository.getNodeProgressUpdated().subscribe {
            if (WelcomeMode.RESTORE == state.mode) {
                if (it.total == 0) {
//                    finishNodeProgressSubscription()
                } else {
                    view?.updateProgress(it, state.mode)

                    if (it.done == it.total) {
                        finishNodeProgressSubscription()
                    }
                }
            }
        }

        nodeConnectionFailedSubscription = repository.getNodeConnectionFailed().subscribe {
            when (state.mode) {
                WelcomeMode.OPEN -> {
                    view?.updateProgress(OnSyncProgressData(1, 1), state.mode)
                    showWallet()
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
                else -> {
                    //for now do nothing
                }
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
    }

    override fun getSubscriptions(): Array<Disposable>? {
        return arrayOf(syncProgressUpdatedSubscription, nodeConnectionFailedSubscription, nodeProgressUpdatedSubscription, nodeStoppedSubscription)
    }

    override fun onDestroy() {
        importRecoverySubscription.dispose()
        downloadSubscription.dispose()
        super.onDestroy()
    }

    private fun showWallet() {
        //sometimes lib notifies us few times about end of progress
        //so we need to unsubscribe from events to prevent unexpected behaviour
        disposable.dispose()
        view?.showWallet()
    }

    private fun finishNodeProgressSubscription() {
        //sometimes lib notifies us few times about end of progress
        //so we need to unsubscribe from events to prevent unexpected behaviour
        nodeProgressUpdatedSubscription.dispose()
        isNodeSyncFinished = true
    }

    private fun clearWalletProgress() {
        repository.removeWallet()
        repository.removeNode()
    }

    private fun cancelRestore() {
        shouldCloseWallet = true
        importRecoverySubscription.dispose()
        downloadSubscription.dispose()
        repository.closeWallet()
        view?.navigateToCreateFragment()
    }

    override fun hasBackArrow(): Boolean? = false
}
