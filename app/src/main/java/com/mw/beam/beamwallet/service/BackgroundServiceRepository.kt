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

package com.mw.beam.beamwallet.service

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.subjects.Subject

class BackgroundServiceRepository: BaseRepository() {
    fun isWalletRunning(): Boolean {
        return Api.isWalletRunning()
    }

    fun isInit():Boolean {
        return Api.isWalletInitialized(AppConfig.DB_PATH)
    }

    fun getPassword(): String? {
        return PreferencesManager.getString(PreferencesManager.KEY_PASSWORD)
    }

    fun getTxStatus(): Observable<OnTxStatusData> {
        return getResult(WalletListener.obsOnTxStatus, "getTxStatus") { wallet?.getWalletStatus() }
    }
}