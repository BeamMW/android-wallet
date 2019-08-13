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
import android.content.ClipData
import android.content.ClipboardManager

/**
 * Created by vain onnellinen on 3/14/19.
 */
fun List<*>.prepareForLog() = this.joinToString { it.toString() }

enum class WelcomeMode {
    OPEN, CREATE, RESTORE, RESTORE_AUTOMATIC, CHANGE_PASS
}

enum class NetworkStatus {
    ONLINE, OFFLINE, UPDATING
}

enum class Status(val value: Int) {
    STATUS_OK(0), STATUS_ERROR(-1);

    companion object {
        private val map: MutableMap<Int, Status> = java.util.HashMap()

        init {
            values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int?): Status {
            return map[type] ?: STATUS_ERROR
        }
    }
}

enum class NodeConnectionError(val value: Int) {
    NODE_PROTOCOL_BASE(0), NODE_PROTOCOL_INCOMPATIBLE(1), CONNECTION_BASE(2),
    CONNECTION_TIMED_OUT(3), CONNECTION_REFUSED(4), CONNECTION_HOST_UNREACHED(5),
    CONNECTION_ADDR_IN_USE(6), TIME_OUT_OF_SYNC(7), INTERNAL_NODE_START_FAILED(8),
    HOST_RESOLVED_ERROR(9);

    companion object {
        private val map: MutableMap<Int, NodeConnectionError> = java.util.HashMap()

        init {
            values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int?): NodeConnectionError {
            return map[type] ?: CONNECTION_REFUSED
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



