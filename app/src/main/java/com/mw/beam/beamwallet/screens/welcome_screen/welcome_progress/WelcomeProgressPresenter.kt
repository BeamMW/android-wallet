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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.NodeConnectionError
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressPresenter(currentView: WelcomeProgressContract.View, currentRepository: WelcomeProgressContract.Repository, private val state: WelcomeProgressState)
    : BasePresenter<WelcomeProgressContract.View, WelcomeProgressContract.Repository>(currentView, currentRepository),
        WelcomeProgressContract.Presenter {
    private lateinit var syncProgressUpdatedSubscription: Disposable
    private lateinit var nodeProgressUpdatedSubscription: Disposable
    private lateinit var nodeConnectionFailedSubscription: Disposable
    private lateinit var nodeStoppedSubscription: Disposable
    private lateinit var failedToStartNodeSubscription: Disposable

    private var isNodeSyncFinished = false

    override fun onCreate() {
        super.onCreate()
        state.mode = view?.getMode() ?: return
        state.password = view?.getPassword() ?: return
        state.seed = view?.getSeed()
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(state.mode)

        if ((state.mode == WelcomeMode.CREATE || state.mode == WelcomeMode.OPEN) && repository.wallet != null) {
            repository.wallet?.syncWithNode()
        }
    }

    override fun onTryAgain() {
        clearWalletProgress()

        if (Status.STATUS_OK == repository.createWallet(state.password, state.seed?.joinToString(separator = ";", postfix = ";"), state.mode)) {
            view?.init(state.mode)
        } else {
            view?.showFailedRestoreAlert()
        }
    }

    override fun onCancel() {
        cancelRestore()
    }

    override fun onOkToCancelRestore() {
        cancelRestore()
    }

    override fun onCancelToCancelRestore() {
        // do nothing
        // alert will be dismissed automatically
    }

    override fun onBackPressed() {
        when (state.mode) {
            WelcomeMode.RESTORE -> {
                view?.showCancelRestoreAlert()
            }
            WelcomeMode.CREATE, WelcomeMode.OPEN -> {
                //for now do nothing
            }
        }
    }

    override fun initSubscriptions() {
        syncProgressUpdatedSubscription = repository.getSyncProgressUpdated().subscribe {
            if (WelcomeMode.RESTORE != state.mode) {
                if (it.total == 0) {
                    view?.updateProgress(OnSyncProgressData(1, 1), state.mode)
                    showWallet()
                } else {
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
                    finishNodeProgressSubscription()
                } else {
                    view?.updateProgress(it, state.mode)

                    if (it.done == it.total) {
                        finishNodeProgressSubscription()
                    }
                }
            }
        }

        nodeConnectionFailedSubscription = repository.getNodeConnectionFailed().subscribe {
            if (state.mode == WelcomeMode.OPEN && repository.wallet != null) {
                when (it) {
                    NodeConnectionError.HOST_RESOLVED_ERROR -> view?.showIncorrectNodeMessage()
                    else -> view?.showNoInternetMessage()
                }

                repository.closeWallet()
                view?.logOut()
            }
        }

        nodeStoppedSubscription = repository.getNodeStopped().subscribe {
            repository.removeNode()

            if (Status.STATUS_OK == repository.openWallet(state.password)) {
                view?.showWallet()
            } else {
                view?.showSnackBar(Status.STATUS_ERROR)
                view?.logOut()
            }
        }

        failedToStartNodeSubscription = repository.getFailedNodeStart().subscribe {
            view?.showFailedRestoreAlert()
        }
    }

    override fun getSubscriptions(): Array<Disposable>? {
        return arrayOf(syncProgressUpdatedSubscription, nodeConnectionFailedSubscription, nodeProgressUpdatedSubscription, nodeStoppedSubscription)
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
        repository.closeWallet()
        repository.removeWallet()
        repository.removeNode()
    }

    private fun cancelRestore() {
        clearWalletProgress()
        view?.logOut()
    }

    override fun hasBackArrow(): Boolean? = false
}
