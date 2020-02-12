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
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.service.BackgroundService
import java.util.concurrent.TimeUnit
import java.io.File
import java.util.*
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.R
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 *  10/1/18.
 */
class App : Application() {

    var subOnStatusResume: Subject<Any?> = PublishSubject.create<Any?>().toSerialized()

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        lateinit var self: App

        private const val BACKGROUND_JOB_ID = 71614
        var isDarkMode = false
        var showNotification = true
        var isAuthenticated = false
        var intentTransactionID: String? = null
        var isAppRunning = false
        var is24HoursTimeFormat: Boolean? = null
        var isNeedOpenScanner: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver{
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume(){
                is24HoursTimeFormat = android.text.format.DateFormat.is24HourFormat(applicationContext)
                subOnStatusResume.onNext(0)

                if (isAuthenticated) {
                    AppManager.instance.updateAllData()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause(){
                LockScreenManager.inactiveDate = System.currentTimeMillis()
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

        self = this

        AppConfig.DB_PATH = filesDir.absolutePath
        AppConfig.LOG_PATH = AppConfig.DB_PATH + "/logs"
        AppConfig.ZIP_PATH = AppConfig.LOG_PATH + "/logs.zip"
        AppConfig.TRANSACTIONS_PATH = AppConfig.DB_PATH + "/transactions"
        AppConfig.CACHE_PATH = AppConfig.DB_PATH + "/cache"

        if (PreferencesManager.getBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE)) {
            PreferencesManager.putString(PreferencesManager.KEY_NODE_ADDRESS, "")
            PreferencesManager.putBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true);
            PreferencesManager.putBoolean(PreferencesManager.KEY_UNFINISHED_RESTORE, false);
            removeDatabase()
        }

        checkRecoverDataBase()

        LocaleHelper.loadLocale()

        if (PreferencesManager.getString(PreferencesManager.KEY_DEFAULT_LOGS) == null) {
            PreferencesManager.putString(PreferencesManager.KEY_DEFAULT_LOGS,PreferencesManager.KEY_DEFAULT_LOGS)
            PreferencesManager.putLong(PreferencesManager.KEY_LOGS,5L)
        }

        XLog.init(LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(AppConfig.APP_TAG)
                .build(),
                FilePrinter
                        .Builder(AppConfig.LOG_PATH)
                        .fileNameGenerator(DateFileNameGenerator())
                        .backupStrategy(NeverBackupStrategy())
                        .flattener(PatternFlattener(AppConfig.LOG_PATTERN))
                        .build())

        clearLogs()

        if(PreferencesManager.getBoolean(PreferencesManager.KEY_BACKGROUND_MODE,false)) {
            App.self.startBackgroundService()
        }
    }

    fun showFaceIdPrompt(fromFragment:Fragment,title:String,cancel:String? = null, resultCallback: (result: com.mw.beam.beamwallet.core.views.Status) -> Unit) {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(fromFragment, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if(errorCode == 13 || cancel == errString)
                        {
                            resultCallback.invoke(com.mw.beam.beamwallet.core.views.Status.CANCEL)
                        }
                        else{
                            resultCallback.invoke(com.mw.beam.beamwallet.core.views.Status.ERROR)
                        }
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        resultCallback.invoke(com.mw.beam.beamwallet.core.views.Status.SUCCESS)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        resultCallback.invoke(com.mw.beam.beamwallet.core.views.Status.FAILED)
                    }
                })

        var cancelText = getString(R.string.enter_your_password)
        if(cancel!=null) {
            cancelText = cancel
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle("")
                .setNegativeButtonText(cancelText)
                .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun startBackgroundService() {
        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(BACKGROUND_JOB_ID, ComponentName(applicationContext, BackgroundService::class.java))
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setPersisted(true)
                .build()
        jobScheduler.schedule(jobInfo)
    }

    fun stopBackgroundService() {
        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()
    }

    fun clearLogs() {
        val days = PreferencesManager.getLong(PreferencesManager.KEY_LOGS)

        if (days<=0L) {
            return
        }

        val path = AppConfig.LOG_PATH
        val directory = File(path)
        val files = directory.listFiles()

        if (files!=null)
        {
            files.sortBy {
                it.lastModified()
            }

            val date = Calendar.getInstance().time

            var removedFiles = mutableListOf<File>()
            for (i in files.indices) {
                if (files[i].exists()) {
                    val modify = Date(files[i].lastModified())
                    val diff =  date.time - modify.time
                    val numOfDays = (diff / (1000 * 60 * 60 * 24))

                    if(numOfDays>=days) {
                        removedFiles.add(files[i])
                    }
                }
            }

            removedFiles.forEach {
                if(it.exists()) {
                    it.delete()
                }
            }
        }
    }
}
