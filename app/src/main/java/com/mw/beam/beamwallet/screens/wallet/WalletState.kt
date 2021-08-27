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

package com.mw.beam.beamwallet.screens.wallet

import android.util.Log
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.utils.CalendarUtils

/**
 *  12/4/18.
 */
class WalletState {
    var walletStatus: WalletStatus? = null

    var privacyMode = false

    fun getTransactions() = AppManager.instance.getTransactions().sortedByDescending { it.createTime }.take(4)

    fun getAssets(): List<Asset> {
        val assets = arrayListOf<Asset>()
        assets.addAll(AssetManager.instance.filteredDataUsed())

        val index = assets.indexOfFirst {
            it.isBeam()
        }

        if (index != 0) {
            val beam = assets[index]
            assets.removeAt(index)
            assets.add(0, beam)
        }

        return assets.take(4)
    }
}
