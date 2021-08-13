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

package com.mw.beam.beamwallet.screens.max_privacy_details

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import java.lang.Math.floor


class MaxPrivacyDetailPresenter(view: MaxPrivacyDetailContract.View?, repository: MaxPrivacyDetailContract.Repository)
    : BasePresenter<MaxPrivacyDetailContract.View, MaxPrivacyDetailContract.Repository>(view, repository), MaxPrivacyDetailContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()

        var utxos = AppManager.instance.getUtxos().filter {
            it.keyType == UtxoKeyType.Shielded && it.status == UtxoStatus.Maturing
        }

        val asset = view?.getAssetId() ?: -1
        if (asset!=-1) {
            utxos = utxos.filter {
                it.assetId == asset
            }
        }

        utxos.forEach {
            val time = AppManager.instance.wallet?.getMaturityHours(it.txoID)
            it.time = time ?: 0L
            it.timeLeft = formattedTime(time?.toInt() ?: 0)
        }

        utxos = utxos.sortedBy {
            it.time
        }

        view?.init(utxos)
    }

    override fun onSelectFilter(filter: MaxPrivacyDetailSort) {
        var utxos = AppManager.instance.getUtxos().filter {
            it.keyType == UtxoKeyType.Shielded && it.status == UtxoStatus.Maturing
        }

        val asset = view?.getAssetId() ?: -1
        if (asset!=-1) {
            utxos = utxos.filter {
                it.assetId == asset
            }
        }

        utxos.forEach {
            val time = AppManager.instance.wallet?.getMaturityHours(it.txoID)
            it.time = time ?: 0L
            it.timeLeft = formattedTime(time?.toInt() ?: 0)
        }

        when (filter) {
            MaxPrivacyDetailSort.time_ear -> {
                utxos = utxos.sortedBy {
                    it.time
                }
            }
            MaxPrivacyDetailSort.time_latest -> {
                utxos = utxos.sortedByDescending {
                    it.time
                }
            }
            MaxPrivacyDetailSort.amount_small -> {
                utxos = utxos.sortedBy {
                    it.amount
                }
            }
            else -> {
                utxos = utxos.sortedByDescending {
                    it.amount
                }
            }
        }

        view?.init(utxos)
    }

    private fun formattedTime(hours: Int): String {
        val f = hours.toDouble()/24.0

        val dd = floor(f)
        var hh = hours.toDouble()
        if (dd != 0.0) {
            hh = hours - dd * 24
        }


        var res = ""

        if (hh.toInt() == 1) {
            res = hh.toInt().toString() + " hour"
        } else if (hh == 0.0){
            res = ""
        } else {
            res = hh.toInt().toString() + " hours"
        }

        if (dd != 0.0) {
            if (dd == 1.0) {
                return "1 day $res"
            } else {
                return "${dd.toInt()} days $res"
            }
        }

        return res;
    }
}