package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO

/**
 * Created by vain onnellinen on 10/2/18.
 */
data class SystemState(private val source: SystemStateDTO) {
    val hash: ByteArray = source.hash
    val height: Long = source.height
}
