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

package com.mw.beam.beamwallet.screens.app_activity

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager

class AppActivityRepository: BaseRepository(), AppActivityContract.Repository {
    override fun sendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long, saveAddress: Boolean, assetId:Int) {
        getResult("sendMoney", " sender: $outgoingAddress\n token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {
            if(AppManager.instance.isToken(token)) {
                val params = AppManager.instance.wallet?.getTransactionParameters(token, false)
                AppManager.instance.setIgnoreAddress(params?.address)
            }
            else  {
                AppManager.instance.removeIgnoredAddress(token)
            }
            wallet?.sendTransaction(outgoingAddress, token, comment ?: "", amount, fee,  assetId)
        }
    }

    override fun cancelSendMoney(token: String) {
        AppManager.instance.lastSendingAddress = ""
    }
}