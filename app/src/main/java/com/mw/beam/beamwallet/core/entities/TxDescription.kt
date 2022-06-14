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
    val amount: Long = if (source.amount >= 0) {
        source.amount
    }
    else {
        source.amount * (-1)
    }
    val fee: Long = source.fee
    val change: Long = source.change
    val minHeight: Long = source.minHeight
    val peerId: String = source.peerId.replaceFirst(Regex("^0+"), "")
    val myId: String = source.myId.replaceFirst(Regex("^0+"), "")
    var message: String = source.message?.capitalize() ?: ""
    val createTime: Long = source.createTime
    val modifyTime: Long = source.modifyTime
    val sender: TxSender = TxSender.fromValue(source.getSenderValue())
    val status: TxStatus = TxStatus.fromValue(source.status)
    val kernelId: String = source.kernelId
    val selfTx: Boolean = source.selfTx
    val failureReason: TxFailureReason = TxFailureReason.fromValue(source.failureReason)
    val failureReasonID: Int = source.failureReason
    val identity: String? = source.identity
    val isShielded = source.isShielded
    val isMaxPrivacy = source.isMaxPrivacy
    val isPublicOffline = source.isPublicOffline
    val token = source.token
    val senderIdentity = source.senderIdentity
    val receiverIdentity = source.receiverIdentity
    val receiverAddress = source.receiverAddress
    val senderAddress = source.senderAddress
    val assetId = source.assetId
    var asset:Asset? = null

    var isDapps:Boolean? = source.isDapps
    var appName:String? = source.appName
    var appID:String? = source.appID
    var contractCids:String? = source.contractCids
    var minConfirmationsProgress:String? = source.minConfirmationsProgress
    var minConfirmations:Int? = source.minConfirmations

    var rate:Long? = null

    fun isInProgress():Boolean {
        return (status == TxStatus.Pending || status==TxStatus.Registered || status==TxStatus.InProgress)
    }

    override fun toString(): String {
        return "\n\nTxDescription(\n id=$id\n amount=$amount\n fee=$fee\n status=${status.name}\n kernelId=$kernelId\n change=$change\n minHeight=$minHeight\n " +
                "peerId=$peerId\n myId=$myId\n message=$message\n createTime=$createTime\n modifyTime=$modifyTime\n sender=${sender.name}\n selfTx=$selfTx\n failureReason=$failureReason)"
    }

    fun hasPaymentProof():Boolean {
        return (sender == TxSender.SENT && status == TxStatus.Completed && !selfTx && isDapps == false);
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

    fun getConfirmation(context: Context): String {
        if (minConfirmationsProgress != null) {
            if (minConfirmationsProgress != "unknown") {
                val first = minConfirmationsProgress!!.split("/").first()
                val last = minConfirmationsProgress!!.split("/").last()
                return if (first == last) {
                    context.getString(R.string.confirmed) + " (" + minConfirmationsProgress!! + ")"
                } else {
                    context.getString(R.string.confirming) + " (" + minConfirmationsProgress!! + ")"
                }
            }
        }
        return  ""
    }


    private fun getStatusEnum():TxStatus {
        if (status == TxStatus.Confirming && ((minConfirmationsProgress == null || minConfirmationsProgress == "unknown"))) {
            return TxStatus.Registered
        }
        return this.status
    }

    fun getStatusString(context: Context) : String {
      var  status = when (getStatusEnum()) {
          TxStatus.Confirming -> {
              context.getString(R.string.confirming).lowercase() + " ($minConfirmationsProgress)"
          }
          TxStatus.Pending ->
          if (selfTx) {
              context.getString(R.string.sending_to_own_address)
          }
          else {
              context.getString(R.string.pending)
          }
          TxStatus.InProgress -> {
              when (sender) {
                  TxSender.RECEIVED -> context.getString(R.string.wallet_status_in_progress_sender)
                  TxSender.SENT -> if (selfTx) {
                      context.getString(R.string.sending_to_own_address)
                  }
                  else {
                       context.getString(R.string.wallet_status_in_progress_receiver)
                  }
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
                  TxFailureReason.TRANSACTION_EXPIRED -> context.getString(R.string.expired_transaction)
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

    fun prefix() = when (sender) {
        TxSender.RECEIVED -> "+"
        TxSender.SENT -> "-"
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
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_canceled_receive_offline)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_receive_failed_offline)
                        TxStatus.Completed, TxStatus.Confirming -> ContextCompat.getDrawable(App.self, R.drawable.ic_receive_offline)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_iprogress_receive_offline)
                    }
                }
                else  {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_canceled_max_online)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_online)
                        TxStatus.Completed, TxStatus.Confirming -> ContextCompat.getDrawable(App.self, R.drawable.ic_received_max_privacy_online)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_privacy_online)
                    }
                }
            }
            else if (sender == TxSender.SENT)
            {
                if(isPublicOffline || isShielded) {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_canceled_offline)
                        TxStatus.Failed -> {
                            when (failureReason) {
                                TxFailureReason.TRANSACTION_EXPIRED -> ContextCompat.getDrawable(App.self, R.drawable.ic_expired)
                                else -> ContextCompat.getDrawable(App.self, R.drawable.ic_send_failed_offline)
                            }
                        }
                        TxStatus.Completed, TxStatus.Confirming -> if (selfTx) {
                            ContextCompat.getDrawable(App.self, R.drawable.ic_sent_own_offline)
                        }
                        else {
                            ContextCompat.getDrawable(App.self, R.drawable.ic_sent_offline)
                        }
                        else ->
                            if(selfTx) {
                                ContextCompat.getDrawable(App.self, R.drawable.ic_send_own_offline)
                            }
                            else {
                                ContextCompat.getDrawable(App.self, R.drawable.ic_icon_sending)
                            }
                    }
                }
                else {
                    return when (this.status) {
                        TxStatus.Cancelled -> ContextCompat.getDrawable(App.self, R.drawable.ic_canceled_max_online)
                        TxStatus.Failed -> ContextCompat.getDrawable(App.self, R.drawable.ic_failed_max_online)
                        TxStatus.Completed, TxStatus.Confirming -> if (selfTx) {
                            ContextCompat.getDrawable(App.self, R.drawable.ic_sent_max_privacy_own)
                        }
                        else {
                            ContextCompat.getDrawable(App.self, R.drawable.ic_sending_max_online)
                        }
                        else ->
                            if(selfTx) {
                                ContextCompat.getDrawable(App.self, R.drawable.ic_seding_max_privacy_own)
                            }
                            else {
                                ContextCompat.getDrawable(App.self, R.drawable.ic_progress_max_online)
                            }
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
                    TxStatus.Completed, TxStatus.Confirming -> ContextCompat.getDrawable(App.self, R.drawable.ic_sent_to_own_address_new)
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
                        TxStatus.Completed, TxStatus.Confirming -> ContextCompat.getDrawable(App.self, R.drawable.ic_received_new)
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
                        TxStatus.Completed, TxStatus.Confirming -> ContextCompat.getDrawable(App.self, R.drawable.ic_sent_new)
                        else -> ContextCompat.getDrawable(App.self, R.drawable.ic_sending_new)
                    }
                }
            }
        }

        return null
    }

    fun getTransactionFailedString(context:Context):String?  {
        val reasons = arrayListOf<String>()
        reasons.add(context.getString(R.string.tx_failure_undefined))
        reasons.add(context.getString(R.string.tx_failure_cancelled))
        reasons.add(context.getString(R.string.tx_failure_receiver_signature_invalid))
        reasons.add(context.getString(R.string.tx_failure_not_registered_in_blockchain))
        reasons.add(context.getString(R.string.tx_failure_not_valid))
        reasons.add(context.getString(R.string.tx_failure_kernel_invalid))
        reasons.add(context.getString(R.string.tx_failure_parameters_not_sended))
        reasons.add(context.getString(R.string.tx_failure_no_inputs))
        reasons.add(context.getString(R.string.tx_failure_addr_expired))
        reasons.add(context.getString(R.string.tx_failure_parameters_not_readed))
        reasons.add(context.getString(R.string.tx_failure_time_out))
        reasons.add(context.getString(R.string.tx_failure_not_signed_by_receiver))
        reasons.add(context.getString(R.string.tx_failure_max_height_to_high))
        reasons.add(context.getString(R.string.tx_failure_invalid_state))
        reasons.add(context.getString(R.string.tx_failure_subtx_failed))
        reasons.add(context.getString(R.string.tx_failure_invalid_contract_amount))
        reasons.add(context.getString(R.string.tx_failure_invalid_sidechain_contract))
        reasons.add(context.getString(R.string.tx_failure_sidechain_internal_error))
        reasons.add(context.getString(R.string.tx_failure_sidechain_network_error))
        reasons.add(context.getString(R.string.tx_failure_invalid_sidechain_response_format))
        reasons.add(context.getString(R.string.tx_failure_invalid_side_chain_credentials))
        reasons.add(context.getString(R.string.tx_failure_not_enough_time_btc_lock))
        reasons.add(context.getString(R.string.tx_failure_create_multisig))
        reasons.add(context.getString(R.string.tx_failure_fee_too_small))
        reasons.add(context.getString(R.string.tx_failure_fee_too_large))
        reasons.add(context.getString(R.string.tx_failure_kernel_min_height))
        reasons.add(context.getString(R.string.tx_failure_loopback))
        reasons.add(context.getString(R.string.tx_failure_key_keeper_no_initialized))
        reasons.add(context.getString(R.string.tx_failure_invalid_asset_id))
        reasons.add(context.getString(R.string.tx_failure_asset_invalid_info))
        reasons.add(context.getString(R.string.tx_failure_asset_invalid_metadata))
        reasons.add(context.getString(R.string.tx_failure_asset_invalid_id))
        reasons.add(context.getString(R.string.tx_failure_asset_confirmation))
        reasons.add(context.getString(R.string.tx_failure_asset_in_use))
        reasons.add(context.getString(R.string.tx_failure_asset_locked))
        reasons.add(context.getString(R.string.tx_failure_asset_small_fee))
        reasons.add(context.getString(R.string.tx_failure_invalid_asset_amount))
        reasons.add(context.getString(R.string.tx_failure_invalid_data_for_payment_proof))
        reasons.add(context.getString(R.string.tx_failure_there_is_no_master_key))
        reasons.add(context.getString(R.string.tx_failure_keeper_malfunctioned))
        reasons.add(context.getString(R.string.tx_failure_aborted_by_user))
        reasons.add(context.getString(R.string.tx_failure_asset_exists))
        reasons.add(context.getString(R.string.tx_failure_asset_invalid_owner_id))
        reasons.add(context.getString(R.string.tx_failure_assets_disabled))
        reasons.add(context.getString(R.string.tx_failure_no_vouchers))
        reasons.add(context.getString(R.string.tx_failure_assets_fork2))
        reasons.add(context.getString(R.string.tx_failure_out_of_slots))
        reasons.add(context.getString(R.string.tx_failure_shielded_coin_fee))
        reasons.add(context.getString(R.string.tx_failure_assets_disabled_receiver))
        reasons.add(context.getString(R.string.tx_failure_assets_disabled_blockchain))
        reasons.add(context.getString(R.string.tx_failure_identity_required))
        reasons.add(context.getString(R.string.tx_failure_cannot_get_vouchers))
        reasons.add(context.getString(R.string.random_node_title))

        if(failureReasonID >= reasons.size) {
            return null
        }
        return reasons[failureReasonID]
    }
}

