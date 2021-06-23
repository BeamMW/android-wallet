package com.mw.beam.beamwallet.core

import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO

class AssetManager {

    companion object {
        private var INSTANCE: AssetManager? = null
        private var colors = arrayOf("#00F6D2","#73FF7C","#FFE75B","#FF746B","#d885ff","#008eff","#ff746b","#91e300","#ffe75a","#9643ff","#395bff","#ff3b3b","#73ff7c","#ffa86c","#ff3abe","#00aee1","#ff5200","#6464ff","#ff7a21","#63afff","#c81f68")

        val instance: AssetManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AssetManager()
                }

                return INSTANCE!!
            }
    }

    var assets = mutableListOf<Asset>()

    init {
        val assetBeam = Asset(0 ,0L, 0L,
                0,0L,0L,0L,0,0,
                0, SystemStateDTO("", 0))
        assetBeam.nthUnitName = "BEAM";
        assetBeam.unitName = "BEAM"
        assetBeam.color = colors[0]
        assetBeam.shortName = "BEAM"
        assetBeam.shortDesc = ""
        assetBeam.longDesc = ""
        assetBeam.site = ""
        assetBeam.paper = ""
        assets.add(assetBeam)
    }


    fun getAsset(id:Int): Asset? {
        return assets.firstOrNull {
            it.assetId == id
        }
    }

}