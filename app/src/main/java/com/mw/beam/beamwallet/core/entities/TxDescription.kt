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
                    private val peerId: String,
                    private val myId: String,
                    val message: ByteArray?,
                    val createTime: Long,
                    val modifyTime: Long,
                    val sender: Boolean,
                    val status: Int) : Parcelable {
    val senderEnum: TxSender
        get() = TxSender.fromValue(sender)

    val statusEnum: TxStatus
        get() = TxStatus.fromValue(status)

    val formattedPeerId = peerId.replaceFirst(Regex("^0+"), "")
    val formattedMyId = myId.replaceFirst(Regex("^0+"), "")

    override fun toString(): String {
        return "TxDescription(id=$id amount=$amount fee=$fee change=$change minHeight=$minHeight " +
                "peerId=$formattedPeerId myId=$formattedMyId message=$message createTime=$createTime modifyTime=$modifyTime sender= ${senderEnum.name} status=${statusEnum.name})"
    }
}
