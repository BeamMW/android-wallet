package com.mw.beam.beamwallet.core.entities

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
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
    var categories: Spannable? = null
    var detailSpannable: Spannable? = null

    constructor(name: String) {
        nId = ""
        pId = ""
        type = NotificationType.News
        this.name = name
    }

     constructor(notification: Notification, hideAmount:Boolean) {
        nId = notification.id
        pId = notification.objId
        isRead = notification.isRead
        type = notification.type
        date = notification.createdTime

        if(type == NotificationType.Version) {
            icon = R.drawable.ic_icon_notifictions_update
            name = App.self.applicationContext.getString(R.string.new_version_available_title)
            name = name?.replace("(version)", pId)
        }
        else if(type == NotificationType.Transaction) {
            val transaction = AppManager.instance.getTransaction(pId)
            if(transaction!=null) {
                val beam = if (hideAmount) "" else transaction.amount.convertToBeamString()
                var address = if (transaction.sender == TxSender.RECEIVED) transaction.peerId else transaction.myId

                if (transaction.sender == TxSender.RECEIVED) {
                    if ((transaction.status == TxStatus.Pending || transaction.status == TxStatus.Registered
                                    || transaction.status == TxStatus.InProgress) && transaction.status != TxStatus.Completed) {
                        icon = R.drawable.ic_icon_notifictions_received
                        name =  App.self.applicationContext.getString(R.string.notification_receive_content_title)
                        val string = App.self.applicationContext.getString(R.string.transaction_receiving_notif_body)
                                .replace("(value)", beam)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                    else if (transaction.status == TxStatus.Cancelled || transaction.status == TxStatus.Failed) {
                        icon = R.drawable.ic_icon_notifictions_failed
                        name =  App.self.applicationContext.getString(R.string.buy_transaction_failed_title)
                        val string = App.self.applicationContext.getString(R.string.transaction_received_notif_body_failed)
                                .replace("(value)", beam)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                    else {
                        icon = R.drawable.ic_icon_notifictions_received
                        name =  App.self.applicationContext.getString(R.string.transaction_received)
                        val string = App.self.applicationContext.getString(R.string.transaction_received_notif_body)
                                .replace("(value)", beam)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                }
                else {
                    if (transaction.status == TxStatus.Cancelled || transaction.status == TxStatus.Failed) {
                        icon = if (transaction.sender == TxSender.RECEIVED) {
                            R.drawable.ic_icon_notifictions_failed_copy
                        } else {
                            R.drawable.ic_icon_notifictions_failed
                        }
                        name =  App.self.applicationContext.getString(R.string.buy_transaction_failed_title)

                        val string = App.self.applicationContext.getString(R.string.transaction_sent_notif_body_failed)
                                .replace("(value)", beam)
                                .replace("(address)", address)
                                .replace("  ", " ")

                        detail = string
                    }
                    else {
                        icon = R.drawable.ic_icon_notifictions_sent
                        name =  App.self.applicationContext.getString(R.string.transaction_sent)
                        val string = App.self.applicationContext.getString(R.string.transaction_sent_notif_body)
                                .replace("(value)", beam)
                                .replace("(address)", address)
                                .replace("  ", " ")
                        detail = string
                    }
                }

                if (detail!=null) {
                    val spannableString = SpannableString(detail)
                    val matcherBeam = Pattern.compile("$beam BEAM").matcher(detail)
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
            name =  App.self.applicationContext.getString(R.string.address_expired)
            detail = pId
            val address = AppManager.instance.getAddress(pId)

            if(address!=null) {

                val tags = TagHelper.getTagsForAddress(pId)
                categories = if(tags.isEmpty()) {
                    null
                } else {
                    tags.createSpannableString(App.self.applicationContext)
                }
            }
        }
    }
}