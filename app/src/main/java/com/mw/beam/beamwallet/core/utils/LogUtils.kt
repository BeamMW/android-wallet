package com.mw.beam.beamwallet.core.utils

import com.mw.beam.beamwallet.BuildConfig

/**
 * Created by vain onnellinen on 10/1/18.
 */
object LogUtils {
    private const val LOG_TAG = "BeamWallet"
    private const val LOG_TAG_ERROR = "ERROR"
    const val LOG_REQUEST = "Request"
    const val LOG_RESPONSE = "Response"

    fun log(message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(LOG_TAG, message)
        }
    }

    fun logD(message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(LOG_TAG, message)
        }
    }

    fun logE(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(LOG_TAG, "$LOG_TAG_ERROR: " + (throwable.message
                    ?: "Unknown exception"))
        }
    }

    fun <T> logResponse(result: T, responseName: String) {
        if (BuildConfig.DEBUG) {
            LogUtils.log(StringBuilder()
                    .append(LogUtils.LOG_RESPONSE)
                    .append(" ")
                    .append(responseName)
                    .append(": ")
                    .append(result.toString())
                    .append("\n")
                    .append("--------------------------")
                    .append("\n").toString())
        }
    }

    fun logErrorResponse(throwable: Throwable, methodName: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(LOG_TAG, "$LOG_TAG_ERROR $LOG_RESPONSE $methodName:" + (throwable.message
                    ?: "Unknown exception"))
        }
    }
}
