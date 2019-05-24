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
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.PatternFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.entities.Wallet
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import com.mw.beam.beamwallet.service.BackgroundService
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by vain onnellinen on 10/1/18.
 */
class App : Application() {

    companion object {
        lateinit var self: App
        //TODO move into correct place
        var wallet: Wallet? = null
        private const val BACKGROUND_JOB_ID = 71614
        var showNotification = true
        var isAuthenticated = false
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.FLAVOR != AppConfig.FLAVOR_MAINNET) {
            Fabric.with(this, Crashlytics(), CrashlyticsNdk())
        }

        when(BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MASTERNET -> AppConfig.EXPLORER_PREFIX = AppConfig.MASTERNET_EXPLORER_PREFIX
            AppConfig.FLAVOR_TESTNET -> AppConfig.EXPLORER_PREFIX = AppConfig.TESTNET_EXPLORER_PREFIX
        }

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        self = this
        AppConfig.DB_PATH = filesDir.absolutePath
        AppConfig.LOG_PATH = AppConfig.DB_PATH + "/logs"
        AppConfig.TRANSACTIONS_PATH = AppConfig.DB_PATH + "/transactions"
        LocaleHelper.loadLocale()

        if (BuildConfig.DEBUG) {
            LeakCanary.install(self)
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
