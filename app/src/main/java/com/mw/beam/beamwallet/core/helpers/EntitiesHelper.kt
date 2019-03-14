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

/**
 * Created by vain onnellinen on 10/3/18.
 */

fun Int.convertToString(): String {
    val hex = Integer.toHexString(this)
    val sb = StringBuilder()

    for (i in 0 until hex.length - 1 step 2) {
        sb.append(Integer.parseInt(hex.substring(i, i + 2), 16).toChar())
    }

    return sb.toString()
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

enum class TxFailureReason(val value: Int) {
    UNKNOWN(0), CANCELLED(1), INVALID_PEER_SIGNATURE(2), FAILED_TO_REGISTER(3),
    INVALID_TRANSACTION(4), INVALID_KERNEL_PROOF(5), FAILED_TO_SEND_PARAMETERS(6),
    NO_INPUTS(7), EXPIRED_ADDRESS_PROVIDED(8), FAILED_TO_GET_PARAMETER(9),
    TRANSACTION_EXPIRED(10), NO_PAYMENT_PROOF(11);

    companion object {
        private val map: MutableMap<Int, TxFailureReason> = java.util.HashMap()

        init {
            TxFailureReason.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int?): TxFailureReason {
            return map[type] ?: UNKNOWN
        }
    }
}

enum class UtxoStatus(val value: Int) {
    Unavailable(0), Available(1), Maturing(2), Outgoing(3), Incoming(4), Change(5), Spent(6);

    companion object {
        private val map: HashMap<Int, UtxoStatus> = HashMap()

        init {
            UtxoStatus.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): UtxoStatus {
            return map[type] ?: throw IllegalArgumentException("Unknown type of UtxoStatus")
        }
    }
}

enum class UtxoKeyType(val value: String) {
    Commission("fees"), Coinbase("mine"), Regular("norm"), Change("chng"),
    Kernel("kern"), Kernel2("kerM"), Identity("iden"),
    ChildKey("SubK"), Bbs("BbsM"), Decoy("dcoy");

    companion object {
        private val map: HashMap<String, UtxoKeyType> = HashMap()

        init {
            UtxoKeyType.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: String): UtxoKeyType {
            return map[type] ?: throw IllegalArgumentException("Unknown type of UtxoKeyType")
        }
    }
}

enum class ExpirePeriod(val value: Long) {
    DAY(86400), NEVER(0)
}

enum class ChangeAction(val value: Int) {
    ADDED(0), REMOVED(1), UPDATED(2), RESET(3);

    companion object {
        private val map: HashMap<Int, ChangeAction> = HashMap()

        init {
            ChangeAction.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): ChangeAction {
            return map[type] ?: throw IllegalArgumentException("Unknown type of ChangeAction")
        }
    }
}
