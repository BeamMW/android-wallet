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

import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.entities.Wallet
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.core.utils.LogUtils
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/1/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
open class BaseRepository : MvpRepository {

    override var wallet: Wallet? = null
        get() = App.wallet

    override fun closeWallet() {
        getResult({
            if (Api.isWalletRunning()) {
                Api.closeWallet()
            }
        }, object {}.javaClass.enclosingMethod.name)
    }

    override fun getNodeConnectionStatusChanged(): Subject<Boolean> {
        return getResult({}, WalletListener.subOnNodeConnectedStatusChanged, object {}.javaClass.enclosingMethod.name)
    }

    override fun getNodeConnectionFailed(): Subject<Any> {
        return getResult({}, WalletListener.subOnNodeConnectionFailed, object {}.javaClass.enclosingMethod.name)
    }

    override fun getSyncProgressUpdated(): Subject<OnSyncProgressData> {
        return getResult({}, WalletListener.subOnSyncProgressUpdated, object {}.javaClass.enclosingMethod.name)
    }

    fun <T> getResult(block: () -> Unit, subject: Subject<T>, requestName: String, additionalInfo: String = ""): Subject<T> {
        LogUtils.log(StringBuilder()
                .append(LogUtils.LOG_REQUEST)
                .append(" ")
                .append(requestName)
                .append("\n")
                .append(additionalInfo)
                .append("\n")
                .append("--------------------------")
                .append("\n").toString())
        block.invoke()
        return subject
    }

    fun getResult(block: () -> Unit, requestName: String, additionalInfo: String = "") {
        LogUtils.log(StringBuilder()
                .append(LogUtils.LOG_REQUEST)
                .append(" ")
                .append(requestName)
                .append("\n")
                .append(additionalInfo)
                .append("\n")
                .append("--------------------------")
                .append("\n").toString())
        block.invoke()
    }
}
