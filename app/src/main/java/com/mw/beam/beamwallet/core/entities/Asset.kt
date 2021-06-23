package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO


class Asset(val assetId: Int,
            var available: Long,
            var receiving: Long,
            var sending: Long,
            var maturing: Long,
            var shielded: Long,
            var maxPrivacy: Long,
            var updateLastTime: Long,
            var updateDone: Int,
            var updateTotal: Int,
            var system: SystemStateDTO) {

    var unitName: String = ""
    var nthUnitName: String = ""
    var shortName: String = ""
    var shortDesc: String = ""
    var longDesc: String = ""
    var name: String = ""
    var site: String = ""
    var paper: String = ""
    var color:String = ""
}