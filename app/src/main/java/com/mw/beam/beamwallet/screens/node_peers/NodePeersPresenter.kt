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

package com.mw.beam.beamwallet.screens.node_peers

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import io.reactivex.disposables.Disposable

class NodePeersPresenter(currentView: NodePeersContract.View, currentRepository: NodePeersContract.Repository)
    : BasePresenter<NodePeersContract.View, NodePeersContract.Repository>(currentView, currentRepository),
        NodePeersContract.Presenter {

    private lateinit var statusSubscription: Disposable

    override fun initSubscriptions() {
        super.initSubscriptions()

        // The status line, the last-seen stamp and the height all move with the connection,
        // so the screen redraws on the same events the toolbar listens to.
        statusSubscription = AppManager.instance.subOnNetworkStatusChanged.subscribe {
            AppActivity.self.runOnUiThread {
                updateView()
            }
        }

        updateView()
    }

    private fun updateView() {
        // An own node is not picked from the pool, so there is no pool worth showing.
        val usesPool = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true) ||
                PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)

        view?.updatePeers(activeNode(), if (usesPool) AppManager.instance.nodePool() else emptyList())
    }

    private fun activeNode(): String {
        val saved = PreferencesManager.getString(PreferencesManager.KEY_NODE_ADDRESS)
        return if (saved.isNullOrBlank()) AppConfig.NODE_ADDRESS else saved
    }

    override fun getSubscriptions(): Array<Disposable> = arrayOf(statusSubscription)

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true
}
