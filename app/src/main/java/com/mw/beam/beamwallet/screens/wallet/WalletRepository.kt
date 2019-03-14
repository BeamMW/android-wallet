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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.methodName
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletRepository : BaseRepository(), WalletContract.Repository {

    override fun getWalletStatus(): Subject<WalletStatus> {
        return getResult(WalletListener.subOnStatus, object {}.methodName())
    }

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult(WalletListener.subOnTxStatus, object {}.methodName()) {
            wallet?.getWalletStatus()
        }
    }
}
