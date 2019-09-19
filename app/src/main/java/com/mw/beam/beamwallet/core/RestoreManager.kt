package com.mw.beam.beamwallet.core

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import io.reactivex.subjects.PublishSubject
import java.io.File

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

    private var handler = Handler()
    private var isProgressCheckerRunning = false
    private var progressChecker:Runnable? = null

    private var downloadManager: DownloadManager? = null

    private var downloadID: Long = 0

    val subDownloadProgress = PublishSubject.create<OnSyncProgressData>().toSerialized()

    fun startDownload(file: File) {
        val link =  when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MAINNET -> "https://mobile-restore.beam.mw/mainnet/mainnet_recovery.bin"
            AppConfig.FLAVOR_TESTNET -> "https://mobile-restore.beam.mw/testnet/testnet_recovery.bin"
            else -> "https://mobile-restore.beam.mw/masternet/masternet_recovery.bin"
        }

        val request = DownloadManager.Request(Uri.parse(link))
                .setTitle(App.self.getString(R.string.app_name))
                .setDescription(App.self.getString(R.string.downloading_blockchain_info))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = App.self.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        downloadID = downloadManager!!.enqueue(request)

        startProgressChecker()
    }

    fun stopDownload() {
        downloadManager?.remove(downloadID)

        stopProgressChecker()
    }

    fun checkDownloadStatus(id:Long) {
        if (downloadID == id) {
            val query = DownloadManager.Query()
            query.setFilterById(downloadID)

            val cursor = downloadManager?.query(query)
            if (cursor!=null) {
                if (!cursor.moveToFirst()) {
                    cursor.close()

                    stopProgressChecker()

                    subDownloadProgress.onNext(OnSyncProgressData(-1, 100))

                    return
                }
                do {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    val status = cursor.getInt(columnIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        subDownloadProgress.onNext(OnSyncProgressData(100, 100))
                    }

                } while (cursor.moveToNext())
                cursor.close()
            }

            stopProgressChecker()
        }
    }

    private fun stopProgressChecker() {
        if (isProgressCheckerRunning) {
            downloadID = 0
            handler.removeCallbacks(progressChecker)
            progressChecker = null
            isProgressCheckerRunning = false
        }
    }


    private fun startProgressChecker() {
        if (!isProgressCheckerRunning) {

            progressChecker = object:Runnable {
                override fun run() {
                    try {
                        checkProgress()
                    } finally {
                        handler.postDelayed(progressChecker, 300)
                    }
                }
            }
            progressChecker?.run()

            isProgressCheckerRunning = true
        }
    }


    private fun checkProgress() {
        val query = DownloadManager.Query()
        query.setFilterById(downloadID)

        val cursor = downloadManager?.query(query)

        if (cursor!=null) {

            if (!cursor.moveToFirst()) {

                cursor.close()

                stopProgressChecker()

                return
            }
            do {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                val downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                val status = cursor.getInt(columnIndex)

                if (status == DownloadManager.STATUS_RUNNING) {
                    var progress = ((downloaded * 100L) / total)

                    if (progress>=100) {
                        progress = 99
                    }

                    subDownloadProgress.onNext(OnSyncProgressData(progress.toInt(), 100))
                }


            } while (cursor.moveToNext())
            cursor.close()
        }
    }
}