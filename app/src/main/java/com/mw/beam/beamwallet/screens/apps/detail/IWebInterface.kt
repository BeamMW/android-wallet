package com.mw.beam.beamwallet.screens.apps.detail

import android.util.Log
import android.webkit.JavascriptInterface
import com.mw.beam.beamwallet.core.utils.LogUtils

class IWebInterface {

    var onCallWalletApi: ((String) -> Unit)? = null

    @JavascriptInterface
    fun getStyle(): Style {
        return Style()
    }

    @JavascriptInterface
    fun callWalletApi(json:String) {
        LogUtils.logResponse(json, "callWalletApi")
        onCallWalletApi?.invoke(json)
    }
}

class Style {

}