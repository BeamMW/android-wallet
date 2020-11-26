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

import android.content.Context
import android.util.Log
import com.mw.beam.beamwallet.core.helpers.LockScreenManager
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

/**
 *  10/1/18.
 */
abstract class BasePresenter<T : MvpView, R : MvpRepository>(var view: T?, var repository: R) : MvpPresenter<T> {
    protected lateinit var disposable: CompositeDisposable

    private var nodeConnectionSubscription: Disposable? = null
    private var nodeConnectingSubscription: Disposable? = null
    private var nodeReconnectionSubscription: Disposable? = null

    private var isActivityStopped = false

    override fun onCreate() {
    }

    override fun onViewCreated() {
        view?.initToolbar(view?.getToolbarTitle(), hasBackArrow(), hasStatus())
    }

    override fun onStart() {
        disposable = CompositeDisposable()

        initSubscriptions()

        val subscriptions = getSubscriptions()

        if (subscriptions != null) {
            disposable.addAll(*subscriptions)
        }

        disposable.apply {
            if (nodeConnectionSubscription != null) {
                add(nodeConnectionSubscription!!)
            }
            if (nodeConnectingSubscription != null) {
                add(nodeConnectingSubscription!!)
            }
            if (nodeReconnectionSubscription != null) {
                add(nodeReconnectionSubscription!!)
            }
        }

        isActivityStopped = false

        view?.addListeners()
        view?.registerKeyboardStateListener()
        view?.hideKeyboard()
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    // Why you need to unregister listeners? See https://stackoverflow.com/q/38368391
    override fun onStop() {
        isActivityStopped = true
        disposable.dispose()
        view?.dismissAlert()
        view?.dismissSnackBar()
        view?.clearListeners()
        view?.unregisterKeyboardStateListener()
    }

    override fun onDestroy() {
        detachView()
    }

    override fun onClose() {
        repository.closeWallet()
    }

    override fun getSubscriptions(): Array<Disposable>? = null

    override fun initSubscriptions() {
        view?.configStatus(AppManager.instance.getNetworkStatus())

        nodeConnectionSubscription = AppManager.instance.subOnNetworkStatusChanged.subscribe(){
            AppActivity.self.runOnUiThread {
                view?.configStatus(AppManager.instance.getNetworkStatus())
            }
        }

        nodeConnectingSubscription = AppManager.instance.subOnConnectingChanged.subscribe() {
            AppActivity.self.runOnUiThread {
                view?.configStatus(AppManager.instance.getNetworkStatus())
            }
        }

        nodeReconnectionSubscription = AppManager.instance.subOnOnNetworkStartReconnecting.subscribe() {
            AppActivity.self.runOnUiThread {
                view?.configStatus(NetworkStatus.RECONNECT)
            }
        }
    }

    private fun detachView() {
        view = null
    }

    override fun onStateIsNotEnsured() {
        view?.logOut()
    }

    override fun onUserInteraction(context: Context) {
        LockScreenManager.restartTimer(context)
    }

    override fun isLockScreenEnabled(): Boolean {
        return repository.isLockScreenEnabled()
    }

    override fun hasStatus(): Boolean = false
    override fun hasBackArrow(): Boolean? = true
}
