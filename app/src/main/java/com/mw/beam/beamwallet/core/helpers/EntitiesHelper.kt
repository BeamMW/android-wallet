package com.mw.beam.beamwallet.core.helpers

/**
 * Created by vain onnellinen on 10/3/18.
 */
object EntitiesHelper {
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

    fun convertToBeamAsFloatString(groth: Long): String {
        return String.format("%.10f", groth.toFloat() / 1000000f)
    }

    fun convertToBeam(groth: Long): Long {
        return groth / 1000000L
    }

    fun convertToBeamWithSign(groth: Long, isSent: Boolean): String {
        val result = convertToBeam(groth)
        return if (isSent) "-$result" else "+$result"
    }

    fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            val st = String.format("%02X", b)
            result.append(st)
        }
        return result.toString().toLowerCase()
    }
}
