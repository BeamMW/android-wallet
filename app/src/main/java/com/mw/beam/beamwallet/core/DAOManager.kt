package com.mw.beam.beamwallet.core

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.entities.DAOApp
import java.io.StringReader

object DAOManager {

    var apps = arrayListOf<DAOApp>()

    fun loadApps(context:Context) {
        val url =  when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MAINNET -> "https://apps.beam.mw/appslist.json"
            AppConfig.FLAVOR_TESTNET -> "https://apps-testnet.beam.mw/appslist.json"
            else -> "http://3.19.141.112/app/appslist.json"
        }

        val queue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val gson = Gson()
                val token: TypeToken<List<DAOApp>> = object : TypeToken<List<DAOApp>>() {}

                val reader = JsonReader(StringReader(response))
                reader.isLenient = true

                val array =  gson.fromJson(reader, token.type) as List<DAOApp>

                apps.clear()
                apps.addAll(array)

                apps.forEach {
                    it.support = AppManager.instance.wallet?.appSupported(it.api_version ?: "current",
                        it.min_api_version ?: "")
                }
            },
            {

            })

        queue.add(stringRequest)
    }

    fun getDaoCoreApp():DAOApp {
        val url = when (BuildConfig.FLAVOR) {
            AppConfig.FLAVOR_MASTERNET -> "http://3.19.141.112:80/app/plugin-dao-core/index.html"
            AppConfig.FLAVOR_TESTNET -> "https://apps-testnet.beam.mw/app/dao-core-app/index.html"
            AppConfig.FLAVOR_MAINNET -> "https://apps.beam.mw/app/dao-core-app/index.html"
            else -> ""
        }

        return DAOApp("BeamX DAO","",url,"", "", "", true)
    }
}