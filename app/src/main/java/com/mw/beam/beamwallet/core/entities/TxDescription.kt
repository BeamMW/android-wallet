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
package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.dto.TxDescriptionDTO
import com.mw.beam.beamwallet.core.helpers.TxFailureReason
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 10/2/18.
 */
@Parcelize
class TxDescription(private val source: TxDescriptionDTO) : Parcelable {
    val id: String = source.id
    val amount: Long = source.amount
    val fee: Long = source.fee
    val change: Long = source.change
    val minHeight: Long = source.minHeight
    val peerId: String = source.peerId.replaceFirst(Regex("^0+"), "")
    val myId: String = source.myId.replaceFirst(Regex("^0+"), "")
    val message: String = source.message ?: ""
    val createTime: Long = source.createTime
    val modifyTime: Long = source.modifyTime
    val sender: TxSender = TxSender.fromValue(source.sender)
    val status: TxStatus = TxStatus.fromValue(source.status)
    val kernelId: String = source.kernelId
    val selfTx: Boolean = source.selfTx
    val failureReason: TxFailureReason = TxFailureReason.fromValue(source.failureReason)

    override fun toString(): String {
        return "\n\nTxDescription(\n id=$id\n amount=$amount\n fee=$fee\n status=${status.name}\n kernelId=$kernelId\n change=$change\n minHeight=$minHeight\n " +
                "peerId=$peerId\n myId=$myId\n message=$message\n createTime=$createTime\n modifyTime=$modifyTime\n sender=${sender.name}\n selfTx=$selfTx\n failureReason=$failureReason)"
    }

    val statusString : String = when (status) {
        TxStatus.Pending -> App.self.getString(R.string.pending)
        TxStatus.InProgress -> {
            when (sender) {
                TxSender.RECEIVED -> App.self.getString(R.string.wallet_status_in_progress_sender)
                TxSender.SENT -> App.self.getString(R.string.wallet_status_in_progress_receiver)
            }
        }
        TxStatus.Registered -> {
            when (sender) {
                TxSender.RECEIVED -> App.self.getString(R.string.receiving)
                TxSender.SENT -> App.self.getString(R.string.sending)
            }
        }
        TxStatus.Completed -> {
            if (selfTx) {
                App.self.getString(R.string.completed)
            } else {
                when (sender) {
                    TxSender.RECEIVED -> App.self.getString(R.string.received)
                    TxSender.SENT -> App.self.getString(R.string.sent)
                }
            }
        }
        TxStatus.Cancelled -> App.self.getString(R.string.cancelled)
        TxStatus.Failed -> {
            when (failureReason) {
                TxFailureReason.TRANSACTION_EXPIRED -> App.self.getString(R.string.expired)
                else -> App.self.getString(R.string.failed)
            }
        }
    }.toLowerCase() + " "

    val amountColor = when (sender) {
        TxSender.RECEIVED -> ContextCompat.getColor(App.self, R.color.received_color)
        TxSender.SENT -> ContextCompat.getColor(App.self, R.color.sent_color)
    }

    val statusColor = if (selfTx || TxStatus.Failed == status || TxStatus.Cancelled == status) {
        ContextCompat.getColor(App.self, R.color.common_text_color)
    } else {
        when (sender) {
            TxSender.RECEIVED -> ContextCompat.getColor(App.self, R.color.received_color)
            TxSender.SENT -> ContextCompat.getColor(App.self, R.color.sent_color)
        }
    }

    val currencyImage = when (sender) {
        TxSender.RECEIVED -> ContextCompat.getDrawable(App.self, R.drawable.currency_beam_receive)
        TxSender.SENT -> ContextCompat.getDrawable(App.self, R.drawable.currency_beam_send)
    }
}

