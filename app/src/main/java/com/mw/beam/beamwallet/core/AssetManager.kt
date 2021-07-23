package com.mw.beam.beamwallet.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.dto.AssetInfoDTO
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

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

    var selectedAssetId = 0
    var assets = mutableListOf<Asset>()

    fun loadAssets() : List<Asset> {
        return assets.map { it }.toList()
    }

    init {
        val json = PreferencesManager.getString(PreferencesManager.KEY_ASSETS)

        if (!json.isNullOrBlank()) {
            val g = Gson()
            val token: TypeToken<List<Asset>> = object : TypeToken<List<Asset>>() {}
            val a = g.fromJson(json, token.type) as List<Asset>
            assets.addAll(a)
        }

        if (assets.size == 0) {
            addBeam()
        }
    }

    private fun addBeam() {
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

    fun clear() {
        assets.clear()
        selectedAssetId = 0
        addBeam()
        onChangeAssets()
    }

    fun onChangeAssets() {
        assets.forEach {
            it.color = getColor(it)
            it.image = getImage(it)
            if (it.nthUnitName.isEmpty()) {
                AppManager.instance.wallet?.getAssetInfo(it.assetId)
            }
        }

        val g = Gson()
        val jsonString = g.toJson(assets)
        PreferencesManager.putString(PreferencesManager.KEY_ASSETS, jsonString)
    }

    fun onReceivedAssetInfo(info: AssetInfoDTO) {
        assets.forEach {
            if (info.id == it.assetId) {
                it.unitName = info.unitName
                it.nthUnitName = info.nthUnitName
                it.shortName = info.shortName

                it.shortDesc = info.shortDesc
                it.longDesc = info.longDesc
                it.name = info.name
                it.site = info.site
                it.paper = info.paper
            }
        }

        val g = Gson()
        val jsonString = g.toJson(assets)
        PreferencesManager.putString(PreferencesManager.KEY_ASSETS, jsonString)
    }

    private  fun getColor(asset: Asset):String {
        return colors[getIndex(asset)]
    }

    private  fun getImage(asset: Asset):Int {
        when (getIndex(asset)) {
            0 -> {
                return R.drawable.ic_asset_0
            }
            1 -> {
                return R.drawable.ic_asset_1
            }
            2 -> {
                return R.drawable.ic_asset_2
            }
            3 -> {
                return R.drawable.ic_asset_3
            }
            4 -> {
                return R.drawable.ic_asset_4
            }
            5 -> {
                return R.drawable.ic_asset_5
            }
            6 -> {
                return R.drawable.ic_asset_6
            }
            else -> {
                return 0
            }
        }
    }

    private fun getIndex(asset: Asset):Int {
        return assets.indexOfFirst {
            asset.assetId == it.assetId
        }
    }

    fun getAsset(id:Int): Asset? {
        return assets.firstOrNull {
            it.assetId == id
        }
    }

    fun getAssetName(name:String): Asset? {
        return assets.firstOrNull {
            it.unitName == name
        }
    }

    fun getLastTransaction(id:Int):TxDescription? {
        return AppManager.instance.getTransactions().filter {
            it.assetId == id
        }.sortedByDescending { it.createTime }.firstOrNull()
    }

    fun getAvailable(id:Int):Long {
        val asset = getAsset(id)
        return (asset?.available ?: 0L) + (asset?.shielded ?: 0L)
    }
}