package com.mw.beam.beamwallet.core.helpers

/**
 * Created by vain onnellinen on 10/3/18.
 */


fun Long.convertToBeam() = this / 1000000L
fun Long.convertToGroth() = this * 1000000L
fun Long.convertToBeamAsFloatString() = String.format("%.10f", this.toFloat() / 1000000f)
fun Long.convertToBeamWithSign(isSent: Boolean) = if (isSent) "-${this.convertToBeam()}" else "+${this.convertToBeam()}"

fun ByteArray.toHex(): String {
    val result = StringBuilder()
    this.forEach { result.append(String.format("%02X", it)) }

    return result.toString().toLowerCase()
}

enum class TxSender(val value: Boolean) {
    SENT(true), RECEIVED(false);

    companion object {
        private val map: HashMap<Boolean, TxSender> = HashMap()

        init {
            TxSender.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Boolean): TxSender {
            return map[type] ?: throw IllegalArgumentException("Unknown type of TxSender")
        }
    }
}

enum class TxStatus(val value: Int) {
    Pending(0), InProgress(1), Cancelled(2), Completed(3), Failed(4), Registered(5);

    companion object {
        private val map: HashMap<Int, TxStatus> = HashMap()

        init {
            TxStatus.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): TxStatus {
            return map[type] ?: throw IllegalArgumentException("Unknown type of TxStatus")
        }
    }
}

