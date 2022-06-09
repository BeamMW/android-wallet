package com.mw.beam.beamwallet.core

import android.util.Log
import androidx.multidex.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.dto.AssetInfoDTO
import com.mw.beam.beamwallet.core.entities.dto.SystemStateDTO
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import java.io.StringReader

class AssetManager {

    companion object {
        private var INSTANCE: AssetManager? = null
        private var colors = arrayOf("#72fdff","#2acf1d","#ffbb54","#d885ff","#008eff","#d885ff","#91e300",
            "#ffe75a","#9643ff","#395bff","#ff3b3b","#73ff7c","#ffa86c","#ff3abe","#00aee1","#ff5200","#6464ff","#ff7a21","#63afff","#c81f68")
        private var icons = arrayOf(R.drawable.asset0,R.drawable.asset1,R.drawable.asset2,R.drawable.asset3,R.drawable.asset4,R.drawable.assetbeamx,R.drawable.asset6,R.drawable.asset7,R.drawable.asset8,R.drawable.asset9,R.drawable.asset10,R.drawable.asset11,R.drawable.asset12,R.drawable.asset13,R.drawable.asset14,R.drawable.asset15,R.drawable.asset16,R.drawable.asset17,R.drawable.asset18,R.drawable.asset19)


        val instance: AssetManager
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AssetManager()
                }

                return INSTANCE!!
            }

        fun makeInit() {
            INSTANCE = AssetManager()
        }
    }

    var requestesAssets = arrayListOf<Int>()
    var selectedAssetId = 0
    var assets = arrayListOf<Asset>()
    var filteredAssets = arrayListOf<Asset>()

    fun loadAssets() : List<Asset> {
        return filteredAssets.map { it }.toList()
    }

    fun filteredDataUsed() : List<Asset> {
        val array = filteredAssets.map { it }.toList()
        val filtered = array.filter {
            it.available > 0L || it.lockedSum() > 0L || it.hasInProgressTransactions()
                    || it.isBeam()
        }

        filtered.sortedByDescending { it.dateUsed() }
        return filtered
    }

    fun fetch() {
        requestesAssets.add(0)

        val json = PreferencesManager.getString(PreferencesManager.KEY_ASSETS)

        if (!json.isNullOrBlank()) {
            val g = Gson()
            val token: TypeToken<List<Asset>> = object : TypeToken<List<Asset>>() {}

            val reader = JsonReader(StringReader(json))
            reader.isLenient = true

            val a = g.fromJson(reader, token.type) as List<Asset>
            assets.addAll(a)
        }

        if (assets.size == 0) {
            addBeam()
        }


        val filtered = assets.filter {
            it.available > 0L || it.lockedSum() > 0L || it.hasInProgressTransactions()
                    || it.isBeam()
        }
        filteredAssets.clear()
        filteredAssets.addAll(filtered)
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

        var assetId = 7
        if (BuildConfig.FLAVOR == AppConfig.FLAVOR_MASTERNET) {
            assetId = 31
        }
        else if (BuildConfig.FLAVOR == AppConfig.FLAVOR_TESTNET) {
            assetId = 12
        }

        val assetBeamX = Asset(assetId ,0L, 0L,
            0,0L,0L,0L,0,0,
            0, SystemStateDTO("", 0))
        assetBeamX.nthUnitName = "BEAMX";
        assetBeamX.unitName = "BEAMX"
        assetBeamX.color = "#977dff"
        assetBeamX.shortName = "BEAMX"
        assetBeamX.shortDesc = "BeamX DAO governance token"
        assetBeamX.longDesc = "BEAMX token is a Confidential Asset issued on top of the Beam blockchain with a fixed emission of 100,000,000 units (except for the lender of a \"last resort\" scenario). BEAMX is the governance token for the BeamX DAO, managed by the BeamX DAO Core contract. Holders can earn BeamX tokens by participating in the DAO activities: providing liquidity to the DeFi applications governed by the DAO or participating in the governance process."
        assetBeamX.site = "https://www.beamxdao.org/"
        assetBeamX.paper = "https://documentation.beam.mw/overview/beamx-tokenomics"
        assets.add(assetBeamX)
    }

    fun beamXId():Int {
        var assetId = 7
        if (BuildConfig.FLAVOR == AppConfig.FLAVOR_MASTERNET) {
            assetId = 31
        }
        else if (BuildConfig.FLAVOR == AppConfig.FLAVOR_TESTNET) {
            assetId = 12
        }
        return assetId
    }

    fun clear() {
        assets.clear()
        filteredAssets.clear()
        requestesAssets.clear()
        requestesAssets.add(0)
        selectedAssetId = 0
        addBeam()
        onChangeAssets()
    }

    fun onChangeAssets() {
        Log.e("ASSETS","ON ASSET CHANGED")

        assets.forEach {
            it.color = getColor(it)
            it.image = getImage(it)
            if (it.nthUnitName.isEmpty() && !requestesAssets.contains(it.assetId)) {
                requestesAssets.add(it.assetId)
                AppManager.instance.wallet?.getAssetInfo(it.assetId)
            }
        }

        val filtered = assets.filter {
            it.available > 0L || it.lockedSum() > 0L || it.hasInProgressTransactions()
                    || it.isBeam()
        }
        filteredAssets.clear()
        filteredAssets.addAll(filtered)

        val g = Gson()
        val jsonString = g.toJson(assets)
        PreferencesManager.putString(PreferencesManager.KEY_ASSETS, jsonString)
    }

    fun onReceivedAssetInfo(info: AssetInfoDTO) {
        Log.e("ASSETS","AssetInfoDTO")

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

        val filtered = assets.filter {
            it.available > 0L || it.lockedSum() > 0L || it.hasInProgressTransactions()
                    || it.isBeam()
        }
        filteredAssets.clear()
        filteredAssets.addAll(filtered)

        requestesAssets.remove(info.id)
    }

    private  fun getColor(asset: Asset):String {
        return when {
            asset.isBeam() -> {
                return "#00F6D2"
            }
            asset.isBeamX() -> {
                return "#977dff"
            }
            //        else if(asset.isDemoX()) {
            //            return "#00F6D2"
            //        }
            else -> {
                val idx = (asset.assetId % icons.size);
                colors[idx]
            }
        }
    }

    private  fun getImage(asset: Asset):Int {
        return when {
            asset.isBeam() -> {
                R.drawable.ic_asset_0
            }
            asset.isBeamX() -> {
                R.drawable.ic_beamxverified
            }
            else -> {
                val idx = (asset.assetId % icons.size);
                icons[idx];
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

    fun getLastTransaction(id:Int):TxDescription? {
        return AppManager.instance.getTransactions().filter {
            it.assetId == id
        }.sortedByDescending { it.createTime }.firstOrNull()
    }

    fun getAvailable(id:Int):Long {
        val asset = getAsset(id)
        return (asset?.available ?: 0L)
    }
}