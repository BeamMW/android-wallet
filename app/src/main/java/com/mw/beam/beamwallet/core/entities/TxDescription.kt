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

import android.content.Context
import android.graphics.drawable.Drawable
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
 *  10/2/18.
 */
@Parcelize
class TxDescription(val source: TxDescriptionDTO) : Parcelable {
    val id: String = source.id
    val amount: Long = source.amount
    val fee: Long = source.fee
    val change: Long = source.change
    val minHeight: Long = source.minHeight
    val peerId: String = source.peerId.replaceFirst(Regex("^0+"), "")
    val myId: String = source.myId.replaceFirst(Regex("^0+"), "")
    var message: String = source.message ?: ""
    val createTime: Long = source.createTime
    val modifyTime: Long = source.modifyTime
    val sender: TxSender = TxSender.fromValue(source.sender)
    val status: TxStatus = TxStatus.fromValue(source.status)
    val kernelId: String = source.kernelId
    val selfTx: Boolean = source.selfTx
    val failureReason: TxFailureReason = TxFailureReason.fromValue(source.failureReason)
    val identity: String? = source.identity
    val isShielded = source.isShielded
    val isMaxPrivacy = source.isMaxPrivacy
    val isPublicOffline = source.isPublicOffline
    val token = source.token
    val senderIdentity = source.senderIdentity
    val receiverIdentity = source.receiverIdentity
    val receiverAddress = source.receiverAddress
    val senderAddress = source.senderAddress

    fun isInProgress():Boolean {
        return (status == TxStatus.Pending || status==TxStatus.Registered || status==TxStatus.InProgress)
    }

    override fun toString(): String {
        return "\n\nTxDescription(\n id=$id\n amount=$amount\n fee=$fee\n status=${status.name}\n kernelId=$kernelId\n change=$change\n minHeight=$minHeight\n " +
                "peerId=$peerId\n myId=$myId\n message=$message\n createTime=$createTime\n modifyTime=$modifyTime\n sender=${sender.name}\n selfTx=$selfTx\n failureReason=$failureReason)"
    }

    fun getAddressType(context: Context): String {
        return when {
            isPublicOffline -> {
                context.getString(R.string.public_offline)
            }
            isMaxPrivacy -> {
                context.getString(R.string.max_privacy)
            }
            isShielded -> {
                context.getString(R.string.offline)
            }
            else -> context.getString(R.string.regular)
        }
    }

    fun getStatusStringWithoutLocalizable() : String = when (status) {
        TxStatus.Pending -> "Pending"
        TxStatus.InProgress -> {
            when (sender) {
                TxSender.RECEIVED -> "Waiting for sender"
                TxSender.SENT -> "Waiting for receiver"
            }
        }
        TxStatus.Registered -> {
            when {
                TxSender.RECEIVED == sender -> "In progress"
                TxSender.SENT == sender && selfTx -> "Sending to own address"
                TxSender.SENT == sender -> "In progress"
                else -> ""
            }
        }
        TxStatus.Completed -> {
            if (selfTx) {
                "Sent to own address"
            } else {
                when (sender) {
                    TxSender.RECEIVED -> "Received"
                    TxSender.SENT -> "Sent"
                }
            }
        }
        TxStatus.Cancelled -> "Cancelled"
        TxStatus.Failed -> {
            when (failureReason) {
                TxFailureReason.TRANSACTION_EXPIRED -> "Expired"
                else -> "Failed"
            }
        }
    }.toLowerCase() + " "

    fun getStatusString(context: Context) : String {
      var  status = when (status) {
          TxStatus.Pending -> context.getString(R.string.pending)
          TxStatus.InProgress -> {
              when (sender) {
                  TxSender.RECEIVED -> context.getString(R.string.wallet_status_in_progress_sender)
                  TxSender.SENT -> context.getString(R.string.wallet_status_in_progress_receiver)
              }
          }
          TxStatus.Registered -> {
              when {
                  TxSender.RECEIVED == sender -> context.getString(R.string.in_progress)
                  TxSender.SENT == sender && selfTx -> context.getString(R.string.sending_to_own_address)
                  TxSender.SENT == sender -> context.getString(R.string.in_progress)
                  else -> ""
              }
          }
          TxStatus.Completed -> {
              if (selfTx) {
                  context.getString(R.string.sent_to_own_address)
              } else {
                  when (sender) {
                      TxSender.RECEIVED -> context.getString(R.string.received)
                      TxSender.SENT -> context.getString(R.string.sent)
                  }
              }
          }
          TxStatus.Cancelled -> context.getString(R.string.cancelled)
          TxStatus.Failed -> {
              when (failureReason) {
                  TxFailureReason.TRANSACTION_EXPIRED -> context.getString(R.string.expired)
                  else -> context.getString(R.string.failed)
              }
          }
      }.toLowerCase()

        when {
            isPublicOffline -> {
                status = status + " (" + context.getString(R.string.public_offline).toLowerCase() + ")"
            }
            isMaxPrivacy -> {
                status = status + " (" + context.getString(R.string.max_privacy).toLowerCase() + ")"
            }
            isShielded -> {
                status = status + " (" + context.getString(R.string.offline).toLowerCase() + ")"
            }
        }

      return "$status "
    }

    fun amountColor() = when (sender) {
        TxSender.RECEIVED -> ContextCompat.getColor(App.self, R.color.received_color)
        TxSender.SENT -> ContextCompat.getColor(App.self, R.color.sent_color)
    }

    fun statusColor() = when {
        TxStatus.Cancelled == status -> {
            ContextCompat.getColor(App.self, R.color.failed_status_color)
        }
        TxStatus.Failed == status -> {
            when (failureReason) {
                TxFailureReason.TRANSACTION_EXPIRED -> ContextCompat.getColor(App.self, R.color.failed_status_color)
                else -> ContextCompat.getColor(App.self, R.color.common_error_color)
            }
        }
        selfTx -> {
            ContextCompat.getColor(App.self, R.color.common_text_color)
        }
        else -> {
            when (sender) {
                TxSender.RECEIVED -> ContextCompat.getColor(App.self, R.color.received_color)
                TxSender.SENT -> ContextCompat.getColor(App.self, R.color.sent_color)
            }
        }
    }

//    val currencyImage = when (sender) {
//        TxSender.RECEIVED -> ContextCompat.getDrawable(App.self, R.drawable.currency_beam_receive)
//        TxSender.SENT -> ContextCompat.getDrawable(App.self, R.drawable.currency_beam_send)
//    }

    fun statusImage():Drawable?  {
        if (isMaxPrivacy || isPublicOffline || isShielded) {
            if (sender == TxSender.RECEIVED)
            {
                if(isPublicOffline || isShielded) {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_cancelled_max_offline)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_offline)
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_received_max_privacy_offline)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_privacy_offline)
                    }
                }
                else  {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_canceled_max_online)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_online)
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_received_max_privacy_online)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_privacy_online)
                    }
                }
            }
            else if (sender == TxSender.SENT)
            {
                if(isPublicOffline || isShielded) {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_cancelled_max_offline)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_offline)
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_max_offline)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_offline)
                    }
                }
                else {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_canceled_max_online)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_online)
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_sending_max_online)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_online)
                    }
                }
            }
        }
        else {
            when {
                selfTx -> return when (this.status) {
                    TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_canceled_new)
                    TxStatus.Failed -> {
                        when (failureReason) {
                            TxFailureReason.TRANSACTION_EXPIRED -> ContextCompat.getDrawable(App.self, R.drawable.ic_expired)
                            else -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_failed)
                        }
                    }
                    TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_sent_to_own_address_new)
                    else -> ContextCompat.getDrawable(App.self, R.drawable.ic_i_sending_to_own_address_new)
                }
                sender == TxSender.RECEIVED -> {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_receive_canceled_new)
                        TxStatus.Failed -> {
                            when (failureReason) {
                                TxFailureReason.TRANSACTION_EXPIRED -> ContextCompat.getDrawable(App.self, R.drawable.ic_expired)
                                else ->  ContextCompat.getDrawable(App.self, R.drawable.ic_receive_canceled)
                            }
                        }
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_received_new)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_receiving_new)
                    }
                }
                sender == TxSender.SENT -> {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_canceled_new)
                        TxStatus.Failed ->
                        when (failureReason) {
                            TxFailureReason.TRANSACTION_EXPIRED -> ContextCompat.getDrawable(App.self, R.drawable.ic_expired)
                            else ->  ContextCompat.getDrawable(App.self, R.drawable.ic_send_failed)
                        }
                        TxStatus.Completed -> ContextCompat.getDrawable(App.self, R.drawable.ic_sent_new)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_sending_new)
                    }
                }
            }
        }

        return null
    }
}

