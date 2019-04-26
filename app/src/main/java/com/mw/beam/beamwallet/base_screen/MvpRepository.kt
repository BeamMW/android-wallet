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

import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.entities.Wallet
import com.mw.beam.beamwallet.core.helpers.NodeConnectionError
import com.mw.beam.beamwallet.core.helpers.Status
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/8/18.
 */
interface MvpRepository {
    val wallet: Wallet?

    fun getNodeConnectionStatusChanged(): Subject<Boolean>
    fun getNodeConnectionFailed(): Subject<NodeConnectionError>
    fun getSyncProgressUpdated(): Subject<OnSyncProgressData>

    fun isPrivacyModeEnabled(): Boolean
    fun setPrivacyModeEnabled(isEnable: Boolean)

    fun openWallet(pass: String?): Status
    fun closeWallet()
}
