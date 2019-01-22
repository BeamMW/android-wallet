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

package com.mw.beam.beamwallet.welcomeScreen.welcomeCreate

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WelcomeCreatePresenter(currentView: WelcomeCreateContract.View, currentRepository: WelcomeCreateContract.Repository)
    : BasePresenter<WelcomeCreateContract.View, WelcomeCreateContract.Repository>(currentView, currentRepository),
        WelcomeCreateContract.Presenter {

    override fun onCreateWallet() {
        view?.createWallet()
    }

    override fun onRestoreWallet() {
        view?.restoreWallet()
    }

    override fun hasBackArrow(): Boolean? = false
}
