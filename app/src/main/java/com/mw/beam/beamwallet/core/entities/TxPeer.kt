package com.mw.beam.beamwallet.core.entities

/**
 * Created by vain onnellinen on 10/16/18.
 */
data class TxPeer(val walletID: ByteArray,
                  val label: String,
                  val address: String)
