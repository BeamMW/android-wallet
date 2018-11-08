package com.mw.beam.beamwallet.core.entities

import android.os.Parcelable
import com.mw.beam.beamwallet.core.helpers.EntitiesHelper
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
    val senderEnum: EntitiesHelper.TxSender
        get() = EntitiesHelper.TxSender.fromValue(sender)

    val statusEnum: EntitiesHelper.TxStatus
        get() = EntitiesHelper.TxStatus.fromValue(status)

//    constructor(source: Parcel) : this(
//            source.createByteArray(),
//            source.readLong(),
//            source.readLong(),
//            source.readLong(),
//            source.readLong(),
//            source.createByteArray(),
//            source.createByteArray(),
//            source.createByteArray(),
//            source.readLong(),
//            source.readLong(),
//            1 == source.readInt(),
//            source.readInt()
//    )
//
//    override fun describeContents() = 0

//    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
//        writeByteArray(id)
//        writeLong(amount)
//        writeLong(fee)
//        writeLong(change)
//        writeLong(minHeight)
//        writeByteArray(peerId)
//        writeByteArray(myId)
//        writeByteArray(message)
//        writeLong(createTime)
//        writeLong(modifyTime)
//        writeInt((if (sender) 1 else 0))
//        writeInt(status)
//    }
//
//    companion object {
//        @JvmField
//        val CREATOR: Parcelable.Creator<TxDescription> = object : Parcelable.Creator<TxDescription> {
//            override fun createFromParcel(source: Parcel): TxDescription = TxDescription(source)
//            override fun newArray(size: Int): Array<TxDescription?> = arrayOfNulls(size)
//        }
//    }
}
