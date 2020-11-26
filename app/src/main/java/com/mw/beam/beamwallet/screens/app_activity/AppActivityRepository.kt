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
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager

class AppActivityRepository: BaseRepository(), AppActivityContract.Repository {
    override fun sendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long, maxPrivacy: Boolean) {
        getResult("sendMoney", " sender: $outgoingAddress\n token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {

            var sender = outgoingAddress
            var receiver = token

            if (AppManager.instance.wallet?.isToken(outgoingAddress) == true) {
                var params = AppManager.instance.wallet!!.getTransactionParameters(outgoingAddress, false)
                sender = params.address
            }

            if (AppManager.instance.wallet?.isToken(token) == true) {
                var params = AppManager.instance.wallet!!.getTransactionParameters(token, false)
                receiver = params.address
            }

            val address = AppManager.instance.getAddress(receiver)
            val name = address?.label

            wallet?.sendTransaction(sender, token, comment ?: "", amount, fee)

            if(address!=null && !name.isNullOrEmpty()) {
                val dto = address.toDTO()
                dto.label = name
                wallet?.saveAddress(dto, address.isContact)
            }

            removeSenContact(receiver)
        }
    }

    override fun cancelSendMoney(token: String) {
       removeSenContact(token)
    }

    private fun removeSenContact(token:String) {
        android.os.Handler().postDelayed({
            var address:WalletAddress? = null
            TrashManager.getAllData().addresses.forEach {
                if (it.walletID == token)
                {
                    address = it
                }
            }

            if (address!=null) {
                TrashManager.remove(token)
            }
        }, 1000)
    }
}