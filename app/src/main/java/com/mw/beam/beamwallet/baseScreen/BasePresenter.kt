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

package com.mw.beam.beamwallet.baseScreen

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/1/18.
 */
abstract class BasePresenter<T : MvpView, R : MvpRepository>(var view: T?, var repository: R) : MvpPresenter<T> {
    private lateinit var disposable: CompositeDisposable
    private lateinit var nodeConnectionSubscription: Disposable
    private lateinit var nodeConnectionFailedSubscription: Disposable
    private lateinit var syncProgressUpdatedSubscription: Disposable

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
            add(nodeConnectionSubscription)
            add(nodeConnectionFailedSubscription)
        }

        view?.addListeners()
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    // Why you need to unregister listeners? See https://stackoverflow.com/q/38368391
    override fun onStop() {
        disposable.dispose()
        view?.dismissAlert()
        view?.clearListeners()
    }

    override fun onDestroy() {
        detachView()
    }

    override fun getSubscriptions(): Array<Disposable>? = null

    override fun initSubscriptions() {
        nodeConnectionSubscription = repository.getNodeConnectionStatusChanged().subscribe {
            view?.configStatus(it)
        }

        nodeConnectionFailedSubscription = repository.getNodeConnectionFailed().subscribe {
            view?.configStatus(false)
        }

        syncProgressUpdatedSubscription = repository.getSyncProgressUpdated().subscribe {
            view?.configStatus(it.done == it.total)
        }
    }

    private fun detachView() {
        view = null
    }

    override fun hasStatus(): Boolean = false
    override fun hasBackArrow(): Boolean? = true
}
