package com.mw.beam.beamwallet.screens.transaction_details

data class UtxoInfoItem(val type: UtxoType, val amount: Long)

enum class UtxoType {
    Send, Receive, Exchange
}