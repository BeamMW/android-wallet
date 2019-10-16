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

package com.mw.beam.beamwallet.core.utils

import com.elvishew.xlog.XLog

/**
 *  10/1/18.
 */
object LogUtils {
    private const val LOG_TAG = "BeamWallet"
    private const val LOG_TAG_ERROR = "ERROR"
    private const val LOG_RESPONSE = "Response"
    const val LOG_REQUEST = "Request"

    fun log(message: String) {
        android.util.Log.e(LOG_TAG, message)
        XLog.e(message)
    }

    fun <T> logResponse(result: T, responseName: String) {
        log(StringBuilder()
                .append(LOG_RESPONSE)
                .append(" ")
                .append(responseName)
                .append(": ")
                .append(result.toString())
                .append("\n")
                .append("--------------------------")
                .append("\n").toString())
    }

    fun logErrorResponse(throwable: Throwable, methodName: String) {
        log("$LOG_TAG_ERROR $LOG_RESPONSE $methodName:" + (throwable.message
                ?: "Unknown exception"))
    }
}
