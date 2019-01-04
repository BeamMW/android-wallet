package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.helpers.prepareForLog

/**
 * Created by vain onnellinen on 10/15/18.
 */
data class OnTxStatusData(val action: Int, val tx: List<TxDescription>?) {
    override fun toString(): String {
        return "OnTxStatusData(action=$action, tx=${tx?.prepareForLog()})"
    }
}
