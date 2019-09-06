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

package com.mw.beam.beamwallet.core

import android.util.Log
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.mw.beam.beamwallet.core.entities.Wallet
import com.mw.beam.beamwallet.core.network.MobileRestoreService
import com.mw.beam.beamwallet.core.network.getOkHttpDownloadClientBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit
import java.io.File
import okio.Okio
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import android.content.Context.DOWNLOAD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.DownloadManager
import android.net.Uri
import android.content.Context


/**
 *  10/1/18.
 */
object Api {
    private val downloadID: Long = 0

    val subDownloadProgress = PublishSubject.create<OnSyncProgressData>().toSerialized()

    private val restoreService by lazy {
        Retrofit.Builder()
                .baseUrl("https://mobile-restore.beam.mw")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpDownloadClientBuilder(subDownloadProgress).build())
                .build().create(MobileRestoreService::class.java)
    }

    init {
        System.loadLibrary("wallet-jni")
    }

    external fun createWallet(appVersion: String, nodeAddr: String, dbPath: String, pass: String, phrases: String, restore: Boolean = false): Wallet?
    external fun openWallet(appVersion: String, nodeAddr: String, dbPath: String, pass: String): Wallet?
    external fun isWalletInitialized(dbPath: String): Boolean
    external fun createMnemonic(): Array<String>
    external fun getDictionary(): Array<String>
    external fun checkReceiverAddress(address: String?): Boolean
    external fun closeWallet()
    external fun isWalletRunning(): Boolean
    external fun getDefaultPeers(): Array<String>

    fun downloadRestoreFile(file: File): Observable<File> {
        Log.d("Api", "start downloadRestoreFile")
        return when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MAINNET -> restoreService.downloadMainnetRecoveryFile()
            AppConfig.FLAVOR_TESTNET -> restoreService.downloadTestnetRecoveryFile()
            else -> restoreService.downloadMasternetRecoveryFile()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .map { response ->
                    Log.d("Api", "end downloadRestoreFile")

                    val body = response.body()
                    if (body != null) {
                        val sink = Okio.buffer(Okio.sink(file))
                        // you can access body of response
                        sink.writeAll(body.source())
                        sink.close()
                    }

                    subDownloadProgress.onNext(OnSyncProgressData(100, 100))
                    file
                }
    }
}
