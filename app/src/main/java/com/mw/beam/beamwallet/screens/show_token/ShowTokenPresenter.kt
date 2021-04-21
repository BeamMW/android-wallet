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

package com.mw.beam.beamwallet.screens.show_token

import com.mw.beam.beamwallet.base_screen.BasePresenter

class ShowTokenPresenter(currentView: ShowTokenContract.View, currentRepository: ShowTokenContract.Repository, private val state: ShowTokenState)
    : BasePresenter<ShowTokenContract.View, ShowTokenContract.Repository>(currentView, currentRepository),
        ShowTokenContract.Presenter {

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.token = view?.getToken()

        view?.init(state.token ?: return)
    }

    override fun onCopyToken() {
        view?.copyToClipboard(state.token ?: return, "ADDRESS")
    }
}
