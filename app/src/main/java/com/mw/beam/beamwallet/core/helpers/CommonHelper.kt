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

package com.mw.beam.beamwallet.core.helpers

import com.mw.beam.beamwallet.core.AppConfig
import java.io.File

/**
 * Created by vain onnellinen on 3/14/19.
 */
fun List<*>.prepareForLog() = this.joinToString { it.toString() }

enum class WelcomeMode {
    OPEN, CREATE, RESTORE
}

enum class NetworkStatus {
    ONLINE, OFFLINE, UPDATING
}

enum class Status(val value: Int) {
    STATUS_OK(0), STATUS_ERROR(-1);

    companion object {
        private val map: MutableMap<Int, Status> = java.util.HashMap()

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
    File(AppConfig.DB_PATH, AppConfig.DB_FILE_NAME).delete()
}

fun removeNodeDatabase() {
    File(AppConfig.DB_PATH, AppConfig.NODE_DB_FILE_NAME).delete()
    File(AppConfig.DB_PATH, AppConfig.NODE_JOURNAL_FILE_NAME).delete()
}
