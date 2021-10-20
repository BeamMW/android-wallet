package com.mw.beam.beamwallet.core.entities

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.DAOManager
import com.mw.beam.beamwallet.core.helpers.*
import java.util.regex.Pattern

class NotificationItem  {
    var nId: String
    var pId: String
    var isRead: Boolean = false
    var date: Long = 0L
    var type: NotificationType
    var name: String? = null
    var icon: Int? = null
    var detail: String? = null
    var detailSpannable: Spannable? = null

    var newTitle: String? = null
    var newDetail: Spannable? = null
    var newIcon: String? = null

    constructor(name: String) {
        nId = ""
        pId = ""
        type = NotificationType.News
        this.name = name
    }

    constructor(notification: Notification, context: Context?) {
        nId = notification.id
        pId = notification.objId
        isRead = notification.isRead
        type = notification.type
        date = notification.createdTime

        val cntx = context ?: App.self.applicationContext

        if(type == NotificationType.Version) {
            icon = R.drawable.ic_icon_notifictions_update
            name = cntx.getString(R.string.new_version_available_title)
            name = name?.replace("(version)", pId)
        }
        else if(type == NotificationType.Transaction) {
            val transaction = AppManager.instance.getTransaction(pId)
            if(transaction!=null) {
                val asset = AssetManager.instance.getAsset(transaction.assetId)
                val assetName = (asset?.unitName ?: "").trimAddress()

                val amountString = transaction.amount.convertToAssetString(assetName)
                var address = if (transaction.sender == TxSender.RECEIVED) transaction.peerId else transaction.myId

                address = if (transaction.isDapps == true) {
                    transaction.appName ?: ""
                } else {
                    address.trimAddress()
                }

                if (transaction.isDapps == true) {
                    address = address + ": " + cntx.getString(R.string.send_money)
                    newTitle = transaction.appName ?: ""

                    if (transaction.status == TxStatus.InProgress || transaction.status == TxStatus.Registered) {
                        val app = (DAOManager.apps.firstOrNull {
                            it.name == newTitle
                        })

                        icon = R.drawable.ic_icon_contract_completed
                        name = cntx.getString(R.string.dapp_transaction_completed)
                        detail = transaction.message
                        //cntx.getString(R.string.dapss_transaction_received_notif_body, address, cntx.getString(R.string.send_money_completed))
                        newIcon = app?.icon

                        if (transaction.sender == TxSender.RECEIVED) {
                            val detail = cntx.getString(R.string.withdrawing, amountString)
                            val spannableString = SpannableString(detail)
                            val matcherBeam = Pattern.compile("$amountString").matcher(detail)
                            if (matcherBeam.find()) {
                                spannableString.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    matcherBeam.start(), matcherBeam.end(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            newDetail = spannableString
                        }
                        else {
                            val detail = cntx.getString(R.string.depositing, amountString)
                            val spannableString = SpannableString(detail)
                            val matcherBeam = Pattern.compile("$amountString").matcher(detail)
                            if (matcherBeam.find()) {
                                spannableString.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    matcherBeam.start(), matcherBeam.end(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            newDetail = spannableString
                        }
                    }
                    else if (transaction.status == TxStatus.Failed || transaction.status == TxStatus.Cancelled) {
                        icon = R.drawable.ic_icon_contract_failed
                        name = cntx.getString(R.string.dapp_transaction_failed)
                        detail = transaction.message
                        //cntx.getString(R.string.dapss_transaction_received_notif_body, address, cntx.getString(R.string.send_money_failed))
                    }
                    else if(transaction.status == TxStatus.Completed) {
                        icon = R.drawable.ic_icon_contract_completed
                        name = cntx.getString(R.string.dapp_transaction_completed)
                        detail = transaction.message
                        //cntx.getString(R.string.dapss_transaction_received_notif_body, address, cntx.getString(R.string.send_money_completed))
                    }
                }
                else if (transaction.sender == TxSender.RECEIVED) {
                    if ((transaction.status == TxStatus.Pending || transaction.status == TxStatus.Registered
                                    || transaction.status == TxStatus.InProgress)
                        && transaction.status != TxStatus.Completed) {

                        icon =  R.drawable.ic_icon_notifictions_received
                        name =  cntx.getString(R.string.notification_receive_content_title)
                        val string = cntx.getString(R.string.transaction_receiving_notif_body)
                                .replace("(value)", amountString)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                    else if (transaction.status == TxStatus.Cancelled || transaction.status == TxStatus.Failed) {
                        icon = if (transaction.isShielded || transaction.isPublicOffline || transaction.isMaxPrivacy) {
                                R.drawable.ic_notifictions_offline_receive_failed
                            }
                            else {
                                R.drawable.ic_notifictions_failed_receive
                            }
                        name =  cntx.getString(R.string.buy_transaction_failed_title)
                        val string = cntx.getString(R.string.transaction_received_notif_body_failed)
                                .replace("(value)", amountString)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                    else {
                         if(transaction.isMaxPrivacy) {
                            icon = R.drawable.ic_notifictions_received_max_privacy
                            name =  cntx.getString(R.string.transaction_received_max_privacy)
                            val string = cntx.getString(R.string.transaction_received_max_privacy_notif_body)
                                    .replace("(value)", amountString)
                                    .replace("(address)", transaction.myId.trimAddress())
                                    .replace("  ", " ")
                            detail = string
                        }
                        else if (transaction.isShielded || transaction.isPublicOffline)
                        {
                            icon = R.drawable.ic_notifictions_received_offline
                            name =  cntx.getString(R.string.transaction_received_from_offline)
                            val string = cntx.getString(R.string.transaction_received_notif_body)
                                    .replace("(value)", amountString)
                                    .replace("(address)", "shielded pool")
                                    .replace("  ", " ")
                            detail = string
                        }
                        else {
                            icon = R.drawable.ic_icon_notifictions_received
                            name =  cntx.getString(R.string.transaction_received)
                            val string = cntx.getString(R.string.transaction_received_notif_body)
                                    .replace("(value)", amountString)
                                    .replace("(address)", address)
                                    .replace("  ", " ")
                            detail = string
                        }
                    }
                }
                else {
                    if (transaction.status == TxStatus.Cancelled || transaction.status == TxStatus.Failed) {
                        name =  cntx.getString(R.string.buy_transaction_failed_title)

                        icon = if (transaction.sender == TxSender.RECEIVED) {
                                R.drawable.ic_icon_notifictions_failed_copy
                            }
                            else {
                                if (transaction.isMaxPrivacy) {
                                    R.drawable.ic_notifictions_failed_sent_max_privacy
                                }
                                else if(transaction.isShielded || transaction.isPublicOffline)
                                {
                                    R.drawable.ic_notifictions_offline_send_failed
                                }
                                else {
                                    R.drawable.ic_icon_notifictions_failed
                                }
                            }

                        val string = cntx.getString(R.string.transaction_sent_notif_body_failed)
                                .replace("(value)", amountString)
                                .replace("(address)", address)
                                .replace("  ", " ")

                        detail = string
                    }
                    else {
                        if (transaction.isMaxPrivacy || transaction.isShielded || transaction.isPublicOffline) {
                            val a = AppManager.instance.getAddress(transaction.myId)
                            address = if (a != null && !a.isContact) {
                                transaction.peerId
                            } else {
                                transaction.myId
                            }

                            if(address.isEmpty()) {
                                address = transaction.receiverAddress
                            }

                            if(transaction.isMaxPrivacy) {
                                name =  cntx.getString(R.string.transaction_sent_max_privacy)
                                icon = R.drawable.ic_notifictions_send_max_privacy
                            }
                            else {
                                icon = R.drawable.ic_notifictions_send_offline
                                name =  cntx.getString(R.string.transaction_send_from_offline)
                            }

                            val string = cntx.getString(R.string.transaction_sent_notif_body)
                                    .replace("(value)", amountString)
                                    .replace("(address)", address)
                                    .replace("  ", " ")
                            detail = string
                        }
                        else {
                            address = transaction.peerId
                            icon = R.drawable.ic_icon_notifictions_sent
                            name =  cntx.getString(R.string.transaction_sent)
                            val string = cntx.getString(R.string.transaction_sent_notif_body)
                                    .replace("(value)", amountString)
                                    .replace("(address)", address)
                                    .replace("  ", " ")
                            detail = string
                        }
                    }
                }

                if (detail!=null) {
                    val spannableString = SpannableString(detail)
                    val matcherBeam = Pattern.compile("$amountString").matcher(detail)
                    if (matcherBeam.find()) {
                        spannableString.setSpan(
                                StyleSpan(Typeface.BOLD),
                                matcherBeam.start(), matcherBeam.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    val matcherAddress = Pattern.compile(address).matcher(detail)
                    if (matcherAddress.find()) {
                        spannableString.setSpan(
                                StyleSpan(Typeface.BOLD),
                                matcherAddress.start(), matcherAddress.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    detailSpannable = spannableString
                }
            }
        }
        else if(type == NotificationType.Address) {
            icon = R.drawable.ic_icon_notifictions_expired
            name =  cntx.getString(R.string.address_expired)
            detail = pId
        }
    }
}
