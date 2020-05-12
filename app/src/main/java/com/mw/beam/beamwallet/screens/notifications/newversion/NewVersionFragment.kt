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

package com.mw.beam.beamwallet.screens.notifications.newversion

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import kotlinx.android.synthetic.main.fragment_new_version.*
import kotlinx.android.synthetic.main.fragment_utxo_details.toolbarLayout


/**
 *  12/20/18.
 */
class NewVersionFragment : BaseFragment<NewVersionPresenter>(), NewVersionContract.View {

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_new_version
    override fun getToolbarTitle(): String? = getString(R.string.notification)
    override fun getNotificationItem(): String? = NewVersionFragmentArgs.fromBundle(arguments!!).version

    override fun init(notificationItem: String?) {
        val toolbarLayout = toolbarLayout
        toolbarLayout.hasStatus = true

        titleView.text = notificationItem?.let { getString(R.string.new_version_available_title).replace("(version)", it) }
        detailView.text = getString(R.string.new_version_available_detail).replace("(version)", BuildConfig.VERSION_NAME)

        btnUpdate.setOnClickListener {
            val pkg = context?.packageName
            try {
                val appStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                appStoreIntent.setPackage("com.android.vending")
                startActivity(appStoreIntent)
            } catch (exception: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
            }
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return NewVersionPresenter(this, NewVersionRepository())
    }
}
