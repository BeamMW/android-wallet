package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 10/2/18.
 */
@Parcelize
class TxDescription(val id: ByteArray,
                    val amount: Long,
                    val fee: Long,
                    val change: Long,
                    val minHeight: Long,
                    val peerId: ByteArray,
                    val myId: ByteArray,
                    val message: ByteArray?,
                    val createTime: Long,
                    val modifyTime: Long,
                    val sender: Boolean,
                    val status: Int) : Parcelable {
    val senderEnum: TxSender
        get() = TxSender.fromValue(sender)

    val statusEnum: TxStatus
        get() = TxStatus.fromValue(status)
}
