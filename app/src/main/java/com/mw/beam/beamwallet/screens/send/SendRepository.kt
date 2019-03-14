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

package com.mw.beam.beamwallet.screens.send

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.methodName
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
class SendRepository : BaseRepository(), SendContract.Repository {

    override fun sendMoney(token: String, comment: String?, amount: Long, fee: Long) {
        getResult(object {}.methodName()) {
            wallet?.sendMoney(token, comment, amount, fee)
        }
    }

    override fun getWalletStatus(): Subject<WalletStatus> {
        return getResult(WalletListener.subOnStatus, object {}.methodName())
    }

    override fun checkAddress(address: String?): Boolean {
        return Api.checkReceiverAddress(address)
    }

    override fun onCantSendToExpired(): Subject<Any> {
        return getResult(WalletListener.subOnCantSendToExpired, object {}.methodName())
    }

    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, object {}.methodName()) {
            wallet?.getAddresses(true)
        }
    }
}
