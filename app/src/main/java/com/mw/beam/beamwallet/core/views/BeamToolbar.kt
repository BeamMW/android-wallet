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

package com.mw.beam.beamwallet.core.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.mw.beam.beamwallet.R
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.marginRight
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.ScreenHelper
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.notifications.newversion.NewVersionFragmentArgs
import com.mw.beam.beamwallet.screens.settings.SettingsFragmentArgs
import com.mw.beam.beamwallet.screens.settings.SettingsFragmentMode
import kotlinx.android.synthetic.main.fragment_receive.*

/**
 *  12/10/18.
 */
class BeamToolbar : LinearLayout {

    var oldStatus = NetworkStatus.ONLINE

    var canShowChangeButton: Boolean = true
        set(value) {
            if(field!=value) {
                configureStatus(oldStatus)
            }
            field = value
        }

    var hasStatus: Boolean = false
        set(value) {
            field = value
            statusLayout.visibility = if (field) View.VISIBLE else View.GONE
        }
    var centerTitle: Boolean = false
        set(value) {
            field = value
            centerTitleView.visibility = if (field) View.VISIBLE else View.GONE
        }
    var hasOffset: Boolean = false
        set(value) {
            field = value
            if (value) {
                val value = ScreenHelper.dpToPx(context, 170)
                changeNodeButton.visibility = View.GONE
                status.setPaddingRelative(0,0,value,0)
            }
            else {
                val value = ScreenHelper.dpToPx(context, 30)
                status.setPaddingRelative(0,0,value,0)
            }
        }
    lateinit var toolbar: Toolbar
    lateinit var status: TextView
    lateinit var statusIcon: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var centerTitleView: TextView
    lateinit var leftTitleView: TextView
    lateinit var changeNodeButton: ImageView
    lateinit var changeNodeButtonClickable: View



    private lateinit var statusLayout: ConstraintLayout

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.toolbar, this)
        toolbar = this.findViewById(R.id.toolbar)
        status = this.findViewById(R.id.connectionStatus)
        statusIcon = this.findViewById(R.id.statusIcon)
        statusLayout = this.findViewById(R.id.statusLayout)
        progressBar = this.findViewById(R.id.progress)
        centerTitleView = this.findViewById(R.id.centerTitle)
        leftTitleView = this.findViewById(R.id.leftTitle)
        changeNodeButton = this.findViewById(R.id.changeNodeButton)
        changeNodeButton.visibility = View.VISIBLE
        changeNodeButtonClickable = this.findViewById(R.id.changeNodeButtonClickable)

        this.orientation = VERTICAL

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.BeamToolbar,
                    0, 0
            )

            hasStatus = a.getBoolean(R.styleable.BeamToolbar_hasStatus, false)
            centerTitle = a.getBoolean(R.styleable.BeamToolbar_centerTitle, false)
        }

        status.text = status.text.toString().toLowerCase()

        toolbar.setNavigationIcon(R.drawable.ic_back)

        configureStatus(AppManager.instance.getNetworkStatus())

        changeNodeButtonClickable.setOnClickListener {
            if (changeNodeButton.visibility == View.VISIBLE && changeNodeButton.alpha == 1.0f) {
                val destinationFragment = R.id.nodeFragment
                val navBuilder = NavOptions.Builder()
                val navigationOptions = navBuilder.setPopUpTo(destinationFragment, true).build()
                AppActivity.self.findNavController(R.id.nav_host).navigate(destinationFragment, bundleOf("password" to null, "seed" to emptyArray<String>()), navigationOptions)
            }
        }
    }

     @SuppressLint("SetTextI18n")
     fun configureStatus(networkStatus: NetworkStatus) {
         if(AppManager.instance.ignoreNetworkStatus) {
             return
         }
         oldStatus = networkStatus

         val mobile = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
         val random = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)

         if(networkStatus == NetworkStatus.RECONNECT) {
             if(App.isDarkMode) {
                 status.setTextColor(context.getColor(R.color.common_text_dark_color_dark))
             }
             else{
                 status.setTextColor(context.getColor(R.color.common_text_dark_color))
             }
             progressBar.indeterminateDrawable.setColorFilter(context.getColor(R.color.category_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
             progressBar.visibility = View.VISIBLE
             statusIcon.visibility = View.INVISIBLE

             when {
                 mobile -> {
                     status.text = context.getString(R.string.reconnect_mobile).toLowerCase()
                 }
                 random -> {
                     status.text = context.getString(R.string.reconnect_random).toLowerCase()
                 }
                 else -> {
                     status.text = context.getString(R.string.reconnecting).toLowerCase()
                 }
             }
         }
        else if (AppManager.instance.isConnecting) {
            progressBar.indeterminateDrawable.setColorFilter(context.getColor(R.color.category_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
            status.setTextColor(context.getColor(R.color.category_orange))

            progressBar.visibility = View.VISIBLE
            statusIcon.visibility = View.INVISIBLE
            status.text = context.getString(R.string.connecting).toLowerCase()
        }
        else{
            when (networkStatus) {
                NetworkStatus.ONLINE -> {
                    handleStatus(true)
                }
                NetworkStatus.OFFLINE -> {
                    handleStatus(false)
                }
                NetworkStatus.UPDATING -> {
                    if(App.isDarkMode) {
                        status.setTextColor(context.getColor(R.color.common_text_dark_color_dark))
                    }
                    else{
                        status.setTextColor(context.getColor(R.color.common_text_dark_color))
                    }

                    val percent = (AppManager.instance.syncProgressData.done.toDouble()/AppManager.instance.syncProgressData.total.toDouble()) * 100.0

                    progressBar.indeterminateDrawable.setColorFilter(context.getColor(R.color.category_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
                    progressBar.visibility = View.VISIBLE
                    statusIcon.visibility = View.INVISIBLE

                    when {
                        mobile -> {
                            val p = percent.toInt().toString() + "%"
                            status.text = context.getString(R.string.updating_mobile, p).toLowerCase()
                        }
                        random -> {
                            status.text = context.getString(R.string.updating).toLowerCase() + " " + percent.toInt().toString() + "%"
                        }
                        else -> {
                            val p = percent.toInt().toString() + "%"
                            status.text = context.getString(R.string.updating_own, p).toLowerCase()
                        }
                    }
                }
            }
        }
    }

    private fun handleStatus(isOnline: Boolean) {
        progressBar.visibility = View.INVISIBLE
        statusIcon.visibility = View.VISIBLE

        var isTrustIcon = false

        if(App.isDarkMode) {
            status.setTextColor(context.getColor(R.color.common_text_dark_color_dark))
        }
        else{
            status.setTextColor(context.getColor(R.color.common_text_dark_color))
        }

        val mobile = PreferencesManager.getBoolean(PreferencesManager.KEY_MOBILE_PROTOCOL, false)
        val random = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, false)

        if (isOnline) {
            if (!ExchangeManager.instance.isCurrenciesAvailable()) {
                val name = ExchangeManager.instance.currentCurrency().shortName()
                when {
                    mobile -> {
                        status.text = context.getString(R.string.exchange_not_available_mobile, name)
                    }
                    random -> {
                        status.text = context.getString(R.string.exchange_not_available_random, name)
                    }
                    else -> {
                        status.text = context.getString(R.string.exchange_not_available_own, name)
                    }
                }

                if (random) {
                    statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.orange_status))
                }
                else {
                   if (AppManager.instance.isMaxPrivacyEnabled()) {
                       isTrustIcon = true
                       statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_icon_tusted_node_status_orange))
                   }
                    else {
                       statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.orange_status))
                   }
                }
            }
            else {
                when {
                    mobile -> {
                        isTrustIcon = true
                        statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tusted_node_status))
                        status.text = context.getString(R.string.online).toLowerCase()
                    }
                    random -> {
                        statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.green_status))
                        status.text = context.getString(R.string.random_node_status).toLowerCase()
                        if (canShowChangeButton) {
                            changeNodeButton.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        if (AppManager.instance.isMaxPrivacyEnabled()) {
                            isTrustIcon = true
                            statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tusted_node_status))
                            status.text = context.getString(R.string.online).toLowerCase()
                        }
                        else {
                            statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.green_status))
                            status.text = context.getString(R.string.own_node_new_status).toLowerCase()
                            if(changeNodeButton.alpha != 0f) {
                                if (canShowChangeButton) {
                                    changeNodeButton.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            val isRandom = PreferencesManager.getBoolean(PreferencesManager.KEY_CONNECT_TO_RANDOM_NODE, true)
            if(!isRandom && !hasOffset) {
                if (canShowChangeButton) {
                    changeNodeButton.visibility = View.VISIBLE
                }
            }
            statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.red_status))

            when {
                mobile -> {
                    status.text = (context.getString(R.string.common_status_error_mobile_node).toLowerCase())
                }
                random -> {
                    status.text = (context.getString(R.string.common_status_error_random_node).toLowerCase())
                }
                else -> {
                    status.text = (context.getString(R.string.common_status_error_own_node, AppConfig.NODE_ADDRESS).toLowerCase())
                }
            }
        }

        if (!isTrustIcon) {
            val paramsStatus = statusIcon.layoutParams as ConstraintLayout.LayoutParams
            paramsStatus.topMargin = ScreenHelper.dpToPx(context, 1)
            statusIcon.layoutParams = paramsStatus
        }
        else {
            val paramsStatus = statusIcon.layoutParams as ConstraintLayout.LayoutParams
            paramsStatus.topMargin = ScreenHelper.dpToPx(context, 4)
            statusIcon.layoutParams = paramsStatus
        }
    }
}
