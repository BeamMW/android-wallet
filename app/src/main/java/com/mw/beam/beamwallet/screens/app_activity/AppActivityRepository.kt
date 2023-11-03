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

import android.os.Handler
import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.AppManager
import android.os.Looper




class AppActivityRepository: BaseRepository(), AppActivityContract.Repository {
    override fun sendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long, saveAddress: Boolean, assetId:Int, isOffline:Boolean) {
        getResult("sendMoney", "token: $token\n comment: $comment\n amount: $amount\n fee: $fee") {
            wallet?.sendTransaction(token, comment ?: "", amount, fee,  assetId, isOffline)
        }

//        if (AppManager.lastSendSavedContact != null) {
//            Handler(Looper.getMainLooper()).postDelayed(Runnable {
//                wallet?.saveAddress(AppManager.lastSendSavedContact!!, true)
////                val id = AppManager.lastSendSavedContact?.walletID ?: ""
////                val name = AppManager.lastSendSavedContact?.label ?: ""
////                if (name.isNotEmpty()) {
////                    val tmpAddress = AppManager.instance.getAddress(id)
////                    if(tmpAddress!=null) {
////                        AppManager.instance.wallet?.updateAddress(id,name,0)
////                    }
////                    wallet?.saveAddress(AppManager.lastSendSavedContact!!, true)
////                }
//            }, 300)
//
//            Handler(Looper.getMainLooper()).postDelayed(Runnable {
//                AppManager.lastSendSavedContact = null
//            }, 400)
//        }
    }

    override fun cancelSendMoney(token: String) {
        AppManager.instance.lastSendingAddress = ""
    }
}