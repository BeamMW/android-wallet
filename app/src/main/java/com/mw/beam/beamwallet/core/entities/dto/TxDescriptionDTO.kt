package com.mw.beam.beamwallet.core.entities.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by vain onnellinen on 1/4/19.
 */
@Parcelize
data class TxDescriptionDTO(var id: ByteArray,
                            var amount: Long,
                            var fee: Long,
                            var change: Long,
                            var minHeight: Long,
                            var peerId: String,
                            var myId: String,
                            var message: ByteArray?,
                            var createTime: Long,
                            var modifyTime: Long,
                            var sender: Boolean,
                            var status: Int) : Parcelable
