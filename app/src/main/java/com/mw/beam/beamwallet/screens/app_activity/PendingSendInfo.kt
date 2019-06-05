package com.mw.beam.beamwallet.screens.app_activity

import java.util.*

data class PendingSendInfo(val token: String, val comment: String?, val amount: Long, val fee: Long) {
    val id by lazy {
        UUID.randomUUID().toString()
    }
}