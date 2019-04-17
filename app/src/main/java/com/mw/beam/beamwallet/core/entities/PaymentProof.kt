package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO

data class PaymentProof(val txId: String, private val paymentInfoDto: PaymentInfoDTO) {
    val paymentInfo: PaymentInfo = PaymentInfo(paymentInfoDto)
}