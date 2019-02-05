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

package com.mw.beam.beamwallet.welcome_screen.welcome_progress

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressPresenter(currentView: WelcomeProgressContract.View, currentRepository: WelcomeProgressContract.Repository, private val state: WelcomeProgressState)
    : BasePresenter<WelcomeProgressContract.View, WelcomeProgressContract.Repository>(currentView, currentRepository),
        WelcomeProgressContract.Presenter {
    private lateinit var syncProgressUpdatedSubscription: Disposable

    override fun onCreate() {
        super.onCreate()
        state.mode = view?.getMode() ?: return
    }

    override fun onViewCreated() {
        super.onViewCreated()
        if ((state.mode == WelcomeMode.CREATE || state.mode == WelcomeMode.OPEN) && repository.wallet != null) {
            repository.wallet?.syncWithNode()
        }
    }

    override fun initSubscriptions() {
        syncProgressUpdatedSubscription = repository.getSyncProgressUpdated().subscribe {
            if (it.total == 0) {
                view?.showWallet()
            } else {
                view?.updateProgress(it, state.mode)

                if (it.done == it.total) {
                    view?.showWallet()
                }
            }
        }
    }

    override fun hasBackArrow(): Boolean? = false
}
