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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.fragment_node_peers.*
import java.text.NumberFormat

/**
 * A read-only look at the node the wallet is talking to and the pool it picks from. Nothing here
 * is editable: choosing a node is what Settings -> Node already does.
 */
class NodePeersFragment : BaseFragment<NodePeersPresenter>(), NodePeersContract.View {

    override fun onControllerGetContentLayoutId() = R.layout.fragment_node_peers
    override fun getToolbarTitle(): String = getString(R.string.node_peers)
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    @SuppressLint("SetTextI18n")
    override fun updatePeers(activeNode: String, pool: List<String>) {
        toolbarLayout.hasStatus = true

        activeNodeAddress.text = activeNode

        val status = AppManager.instance.getNetworkStatus()
        activeNodeStatus.text = when (status) {
            NetworkStatus.ONLINE, NetworkStatus.UPDATING -> getString(R.string.node_peer_connected)
            NetworkStatus.RECONNECT -> getString(R.string.reconnecting)
            NetworkStatus.OFFLINE -> if (AppManager.instance.isConnecting) {
                getString(R.string.connecting)
            }
            else {
                getString(R.string.node_peer_disconnected)
            }
        }
        activeNodeStatus.setTextColor(ContextCompat.getColor(requireContext(), when (status) {
            NetworkStatus.ONLINE, NetworkStatus.UPDATING -> R.color.received_color
            NetworkStatus.RECONNECT -> R.color.colorAccent
            NetworkStatus.OFFLINE -> R.color.common_error_color
        }))

        val lastSeen = AppManager.instance.lastConnectionChangedAt
        if (lastSeen == null) {
            activeNodeLastSeen.visibility = View.GONE
        }
        else {
            activeNodeLastSeen.visibility = View.VISIBLE
            activeNodeLastSeen.text = getString(R.string.node_peer_last_seen, CalendarUtils.fromTimestamp(lastSeen / 1000))
        }

        val height = AppManager.instance.getStatus().system.height
        if (height <= 0) {
            activeNodeHeight.visibility = View.GONE
        }
        else {
            activeNodeHeight.visibility = View.VISIBLE
            activeNodeHeight.text = getString(R.string.blockchain_height) + ": " +
                    NumberFormat.getIntegerInstance(AppConfig.LOCALE).format(height)
        }

        peersContainer.removeAllViews()
        poolTitle.visibility = if (pool.isEmpty()) View.GONE else View.VISIBLE

        pool.forEach { peer ->
            val row = LayoutInflater.from(context).inflate(R.layout.item_node_peer, peersContainer, false)
            row.findViewById<TextView>(R.id.peerAddress).text = peer
            row.findViewById<TextView>(R.id.peerActiveMark).visibility =
                if (peer == activeNode) View.VISIBLE else View.GONE
            peersContainer.addView(row)
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return NodePeersPresenter(this, NodePeersRepository())
    }
}
