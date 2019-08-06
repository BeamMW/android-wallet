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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_progress

import android.annotation.SuppressLint
import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.Api
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.listeners.WalletListener
import com.mw.beam.beamwallet.core.utils.LogUtils
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.io.File

/**
 * Created by vain onnellinen on 1/24/19.
 */
class WelcomeProgressRepository : BaseRepository(), WelcomeProgressContract.Repository {
    private var downloadProgressSubject: Subject<OnSyncProgressData>? = null

    init {
        subscribeToDownloadProgress()
    }

    @SuppressLint("CheckResult")
    private fun subscribeToDownloadProgress() {
        Api.subDownloadProgress.subscribe {
            downloadProgressSubject?.apply {
                if (!hasComplete() && !hasThrowable()) {
                    onNext(it)
                }
            }
        }
    }

    override fun getNodeProgressUpdated(): Subject<OnSyncProgressData> {
        return getResult(WalletListener.subOnNodeSyncProgressUpdated, "getNodeProgressUpdated")
    }

    override fun getNodeStopped(): Subject<Any> {
        return getResult(WalletListener.subOnStoppedNode, "getNodeStopped")
    }

    override fun getFailedNodeStart(): Subject<Any> {
        return getResult(WalletListener.subOnFailedToStartNode, "getFailedNodeStart")
    }

    override fun getNodeThreadFinished(): Subject<Any> {
        return getResult(WalletListener.subOnNodeThreadFinished, "getNodeThreadFinished")
    }

    override fun removeNode() {
        removeNodeDatabase()
    }

    override fun removeWallet() {
        removeDatabase()
    }

    override fun getImportRecoveryState(pass: String?, seed: String?, file: File): Subject<OnSyncProgressData> {
        closeWallet()
        removeWallet()
        createWallet(pass, seed)

        return getResult(WalletListener.subOnImportRecoveryProgress, "importRecovery") {
            wallet?.importRecovery(file.absolutePath)
        }
    }

    override fun createRestoreFile(): File {
        val file = File(AppConfig.CACHE_PATH, "recovery.bin")

        if (!file.parentFile.exists()) {
            file.parentFile.mkdir()
        } else {
            file.parentFile.listFiles().forEach { it.delete() }
        }
        file.createNewFile()

        return file
    }

    @SuppressLint("CheckResult")
    override fun downloadRestoreFile(file: File): Subject<OnSyncProgressData> {
        downloadProgressSubject = PublishSubject.create<OnSyncProgressData>()
        Api.downloadRestoreFile(file).subscribe({ }, { downloadProgressSubject?.onError(it) })
        return downloadProgressSubject!!
    }

    override fun createWallet(pass: String?, seed: String?): Status {
        var result = Status.STATUS_ERROR

        if (!pass.isNullOrBlank() && seed != null) {
            if (Api.isWalletInitialized(AppConfig.DB_PATH)) {
                removeWallet()
                removeNode()
            }

            val nodeAddress = PreferencesManager.getString(PreferencesManager.KEY_NODE_ADDRESS)
            if (!isEnabledConnectToRandomNode() && !nodeAddress.isNullOrBlank()) {
                AppConfig.NODE_ADDRESS = nodeAddress
            } else {
                AppConfig.NODE_ADDRESS = Api.getDefaultPeers().random()
            }

            App.wallet = Api.createWallet(AppConfig.APP_VERSION, AppConfig.NODE_ADDRESS, AppConfig.DB_PATH, pass, seed)

            if (wallet != null) {
                PreferencesManager.putString(PreferencesManager.KEY_PASSWORD, pass)
                result = Status.STATUS_OK
            }
        }

        LogUtils.logResponse(result, "createWallet")
        return result
    }
}