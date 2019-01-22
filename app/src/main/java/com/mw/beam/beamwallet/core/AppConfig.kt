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

import java.io.File
import java.util.*

/**
 * Created by vain onnellinen on 10/1/18.
 */
object AppConfig {
    const val APP_TAG = "BeamWallet"
    const val LOG_CLEAN_TIME: Long = 259200000
    const val LOG_PATTERN = "{d yyyy-MM-dd hh:mm:ss.SSS} {l}/{t}: {m}"
    const val SHARE_TYPE = "text/plain"
    const val SHARE_VALUE = "Logs"
    const val AUTHORITY = "com.mw.beam.beamwallet.fileprovider"
    const val SUPPORT_EMAIL = "tatsiana@beam-mw.com"
    var DB_PATH = ""
    var LOG_PATH = ""
    var DB_FILE_NAME = "wallet.db"
    var NODE_ADDRESS = "ap-node03.testnet.beam.mw:8100"
    var LOCALE: Locale = Locale.US

    enum class Status(val value: Int) {
        STATUS_OK(0), STATUS_ERROR(-1);

        companion object {
            private val map: MutableMap<Int, Status> = HashMap()

            init {
                Status.values().forEach {
                    map[it.value] = it
                }
            }

            fun fromValue(type: Int?): Status {
                return map[type] ?: STATUS_ERROR
            }
        }
    }

    fun removeDatabase() {
        File(DB_PATH, DB_FILE_NAME).delete()
    }
}
