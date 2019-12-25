package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.prepareForLog

data class OnUTXOData(val action: ChangeAction, val utxo: List<Utxo>?) {
    override fun toString(): String {
        return "OnUTXOData(action=$action, tx=${utxo?.prepareForLog()})"
    }
}
