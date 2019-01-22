// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.core

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.entities.Wallet
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import java.util.*

/**
 * Created by vain onnellinen on 10/1/18.
 */
class App : Application() {

    companion object {
        lateinit var self: App
        //TODO move into correct place
        var wallet: Wallet? = null
    }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }

        self = this
        AppConfig.DB_PATH = filesDir.absolutePath
        AppConfig.LOCALE = Locale.getDefault()

        if (BuildConfig.DEBUG) {
            LeakCanary.install(self)
        }
    }
}
