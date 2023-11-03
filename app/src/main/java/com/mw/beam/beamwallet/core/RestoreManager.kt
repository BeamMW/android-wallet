package com.mw.beam.beamwallet.core

import android.net.Uri
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import com.tonyodev.fetch2.Error
import io.reactivex.subjects.PublishSubject
import java.io.File
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2core.DownloadBlock
import java.util.concurrent.TimeUnit


class RestoreManager {

    companion object {
        private var INSTANCE: RestoreManager? = null

        val instance: RestoreManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = RestoreManager()
                }

                return INSTANCE!!
            }
    }


    private var fetch: Fetch? = null
    private var fetchListener:FetchListener? = null

    val subDownloadProgress = PublishSubject.create<OnSyncProgressData>().toSerialized()

    fun startDownload(file: File) {
        val link =  when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MAINNET -> "https://mobile-restore.beam.mw/mainnet/mainnet_recovery.bin"
            AppConfig.FLAVOR_TESTNET -> "https://mobile-restore.beam.mw/testnet/testnet_recovery.bin"
            else -> "https://s3.eu-central-1.amazonaws.com/mobile-restore.beam.mw/dappnet/dappnet_recovery.bin" //"https://mobile-restore.beam.mw/masternet/masternet_recovery.bin"
        }

        val fetchConfiguration = FetchConfiguration.Builder(App.self.baseContext)
                .setDownloadConcurrentLimit(1)
                .build()

        fetch = null
        fetch = Fetch.getInstance(fetchConfiguration)
        fetch?.deleteAll()

        val request = Request(link, Uri.fromFile(file))
        request.priority = Priority.HIGH
        request.networkType = NetworkType.ALL


        fetchListener = object : FetchListener {

            override fun onWaitingNetwork(download: Download) {
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                subDownloadProgress.onNext(OnSyncProgressData(-1, 100))
            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
            }

            override fun onAdded(download: Download) {
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            }

            override fun onCompleted(download: Download) {
                subDownloadProgress.onNext(OnSyncProgressData(100, 100))
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                val progress = download.progress
                if (progress in 1..99) {
                    val time = TimeUnit.MILLISECONDS.toSeconds(etaInMilliSeconds)
                    subDownloadProgress.onNext(OnSyncProgressData(progress, 100, time.toInt()))
                }
            }

            override fun onPaused(download: Download) {
            }

            override fun onResumed(download: Download) {
            }

            override fun onCancelled(download: Download) {
                subDownloadProgress.onNext(OnSyncProgressData(-1, 100))
            }

            override fun onRemoved(download: Download) {
            }

            override fun onDeleted(download: Download) {
            }
        }

        fetch?.addListener(fetchListener!!)

        fetch?.enqueue(request,
                Func<Request> {
                },
                Func<Error> {
                })
    }

    fun stopDownload() {
        if (fetch?.isClosed == false) {
            if (fetchListener!=null) {
                fetch?.removeListener(fetchListener!!)
            }
            fetch?.deleteAll()
            fetch?.close()
        }
    }
}