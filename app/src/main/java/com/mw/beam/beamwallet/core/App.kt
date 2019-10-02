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

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.PatternFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.removeDatabase
import com.mw.beam.beamwallet.service.BackgroundService
//import com.squareup.leakcanary.LeakCanary
import java.util.concurrent.TimeUnit

/**
 *  10/1/18.
 */
class App : Application() {

    companion object {
        lateinit var self: App

        private const val BACKGROUND_JOB_ID = 71614

        var showNotification = true
        var isAuthenticated = false
        var isShowedLockScreen = false
        var intentTransactionID: String? = null
        var isAppRunning = false
        var is24HoursTimeFormat: Boolean? = null
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver{
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume(){
                is24HoursTimeFormat = android.text.format.DateFormat.is24HourFormat(applicationContext)
            }
        })

        when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MASTERNET -> AppConfig.EXPLORER_PREFIX = AppConfig.MASTERNET_EXPLORER_PREFIX
            AppConfig.FLAVOR_TESTNET -> AppConfig.EXPLORER_PREFIX = AppConfig.TESTNET_EXPLORER_PREFIX
        }

        AppConfig.FORK_HEIGHT = when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MAINNET -> AppConfig.MAINNET_FORK_HEIGHT
            AppConfig.FLAVOR_MASTERNET -> AppConfig.MASTERNET_FORK_HEIGHT
            AppConfig.FLAVOR_TESTNET -> AppConfig.TESTNET_FORK_HEIGHT
            else -> 0
        }

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return
//        }

        self = this

        AppConfig.DB_PATH = filesDir.absolutePath
        AppConfig.LOG_PATH = AppConfig.DB_PATH + "/logs"
        AppConfig.ZIP_PATH = AppConfig.LOG_PATH + "/logs.zip"
        AppConfig.TRANSACTIONS_PATH = AppConfig.DB_PATH + "/transactions"
        AppConfig.CACHE_PATH = AppConfig.DB_PATH + "/cache"
        LocaleHelper.loadLocale()

//        if (BuildConfig.DEBUG) {
//            LeakCanary.install(self)
//        }

        if (PreferencesManager.getBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE)) {

            PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, "")
            PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true);

            removeDatabase()
        }

        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(BACKGROUND_JOB_ID, ComponentName(applicationContext, BackgroundService::class.java))
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setPersisted(true)
                .build()

        jobScheduler.schedule(jobInfo)

        XLog.init(LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(AppConfig.APP_TAG)
                .build(),
                FilePrinter
                        .Builder(AppConfig.LOG_PATH)
                        .fileNameGenerator(DateFileNameGenerator())
                        .backupStrategy(NeverBackupStrategy())
                        .cleanStrategy(FileLastModifiedCleanStrategy(AppConfig.LOG_CLEAN_TIME))
                        .flattener(PatternFlattener(AppConfig.LOG_PATTERN))
                        .build())
    }
}
