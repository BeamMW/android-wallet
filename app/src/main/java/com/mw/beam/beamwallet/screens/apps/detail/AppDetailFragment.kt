/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.apps.detail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import android.view.*
import android.webkit.*

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.DAOApp
import com.mw.beam.beamwallet.core.entities.dto.ContractConsentDTO
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.apps.confirm.AppConfirmDialog
import com.mw.beam.beamwallet.screens.timer_overlay_dialog.TimerOverlayDialog

import kotlinx.android.synthetic.main.fragment_app_detail.*
import kotlinx.android.synthetic.main.fragment_app_detail.toolbarLayout
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.toolbar.*

import org.jetbrains.anko.runOnUiThread

class AppDetailFragment : BaseFragment<AppDetailPresenter>(), AppDetailContract.View {

    private val webInterface = IWebInterface()

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppDetailPresenter(this, AppDetailRepository())
    }

    override fun getToolbarTitle(): String = getApp().name ?: ""
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_app_detail
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark_dark)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
    }


    override fun getApp(): DAOApp {
        return AppDetailFragmentArgs.fromBundle(requireArguments()).app
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()

        val showMenu = AppDetailFragmentArgs.fromBundle(requireArguments()).displayMenu
        if (showMenu) {
            (activity as? AppActivity)?.enableLeftMenu(true)
            toolbar.setNavigationIcon(R.drawable.ic_menu)
            toolbar.setNavigationOnClickListener {
                (activity as? AppActivity)?.openMenu()
            }
            toolbarLayout.centerTitle = true
            toolbarLayout.centerTitleView.visibility = View.VISIBLE
        }
    }

    override fun init() {
        val showMenu = AppDetailFragmentArgs.fromBundle(requireArguments()).displayMenu
        if (showMenu) {
            toolbarLayout.centerTitle = true
            toolbarLayout.centerTitleView.visibility = View.VISIBLE
        }
    }

    override fun setJSCommand(command: String) {
        webView.evaluateJavascript(command)  {}
    }

    override fun showConfirmation(info: ContractConsentDTO) {
       App.self.runOnUiThread {
           val dialog = AppConfirmDialog.newInstance(info, getApp()) { confirm, request ->
               if (confirm) {
                   AppManager.instance.wallet?.contractInfoApproved(request)
               }
               else {
                   AppManager.instance.wallet?.contractInfoRejected(request)
               }
           }
           dialog.show(activity?.supportFragmentManager!!, TimerOverlayDialog.getFragmentTag())
       }
    }

    override fun onDestroy() {
        webInterface.onCallWalletApi = null

        super.onDestroy()
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun setupWebView() {
        val app = getApp()

        toolbarLayout.changeNodeButton.alpha = 0f
        toolbarLayout.changeNodeButton.visibility = View.GONE
        toolbarLayout.changeNodeButton.isEnabled = false

        AppManager.instance.wallet?.launchApp(app.name ?: "", app.url ?: "")

        webInterface.onCallWalletApi = { json->
            AppActivity.self.runOnUiThread {
                loadingView?.visibility = View.GONE
                toolbarLayout.changeNodeButton.alpha = 1.0f
                toolbarLayout.changeNodeButton.visibility = View.VISIBLE
                toolbarLayout.changeNodeButton.isEnabled = true
                webView?.alpha = 1f
            }
            AppManager.instance.wallet?.callWalletApi(json)
        }

        webView.alpha = 0f
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {

            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                val hex =  if (App.isDarkMode) {
                     Integer.toHexString(resources.getColor(R.color.colorPrimaryDark_dark, requireContext().theme)).uppercase().substring(2)
                }
                else{
                    Integer.toHexString(resources.getColor(R.color.colorPrimaryDark, requireContext().theme)).uppercase().substring(2)
                }

                webView.evaluateJavascript("window.BEAM.style = window.BEAM.getStyle();") {}
                webView.evaluateJavascript("window.BEAM.style.appsGradientOffset = -174") {}
                webView.evaluateJavascript("window.BEAM.style.appsGradientTop = 56") {}
                webView.evaluateJavascript("window.BEAM.style.navigation_background = \"#000000\"") {}
                webView.evaluateJavascript("window.BEAM.style.background_main = \"#$hex\"") {}
                webView.evaluateJavascript("window.BEAM.style.background_main_top = \"#$hex\"") {}
                webView.evaluateJavascript("window.BEAM.style.content_main = \"#ffffff\"") {}
                webView.evaluateJavascript("window.BEAM.style.background_popup = \"#323232\"") {}
                webView.evaluateJavascript("window.BEAM.style.validator_error = \"#ff625c\"") {}
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.e("WEB LOG", "${message.message()} -- From line " +
                        "${message.lineNumber()} of ${message.sourceId()}")
                return true
            }
        }
        webView.addJavascriptInterface(webInterface, "BEAM")
        webView.clearCache(true)
        webView.loadUrl(app.url ?: "")
    }
}