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

import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.convertToBeamString

object TransactionFields {
    private const val TYPE_FIELD = "Type"
    private const val DATE_TIME_FIELD = "Date | Time"
    private const val AMOUNT_FIELD = "Amount, BEAM"
    private const val STATUS_FIELD = "Status"
    private const val SENDING_ADDRESS_FIELD = "Sending address"
    private const val RECEIVING_ADDRESS_FIELD = "Receiving address"
    private const val TRANSACTION_FEE_FIELD = "Transaction fee, BEAM"
    private const val TRANSACTION_ID_FIELD = "Transaction ID"
    private const val KERNEL_ID_FIELD = "Kernel ID"

    private const val SEND_BEAM_TYPE = "Send BEAM"
    private const val RECEIVE_BEAM_TYPE = "Receive BEAM"

    const val HEAD_LINE = "$TYPE_FIELD," +
            "$DATE_TIME_FIELD," +
            "\"$AMOUNT_FIELD\"," +
            "$STATUS_FIELD," +
            "$SENDING_ADDRESS_FIELD," +
            "$RECEIVING_ADDRESS_FIELD," +
            "\"$TRANSACTION_FEE_FIELD\"," +
            "$TRANSACTION_ID_FIELD," +
            "$KERNEL_ID_FIELD\n"

    fun formatTransaction(txDescription: TxDescription): String {
        val sender = if (txDescription.sender.value) txDescription.myId else txDescription.peerId
        val receiver = if (txDescription.sender.value) txDescription.peerId else txDescription.myId
        return "${if (txDescription.sender.value) SEND_BEAM_TYPE else RECEIVE_BEAM_TYPE} ," +
                "${CalendarUtils.fromTimestampUS(txDescription.modifyTime)} ," +
                "${txDescription.amount.convertToBeamString()} ," +
                "${txDescription.getStatusStringWithoutLocalizable()} ," +
                "$sender ," +
                "$receiver ," +
                "${txDescription.fee.convertToBeamString()} ," +
                "${txDescription.id} ," +
                "${txDescription.kernelId}\n"
    }
}