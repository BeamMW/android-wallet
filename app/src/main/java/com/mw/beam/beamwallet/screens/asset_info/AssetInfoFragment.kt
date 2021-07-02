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

package com.mw.beam.beamwallet.screens.asset_info


import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.screens.app_activity.AppActivity

import kotlinx.android.synthetic.main.fragment_asset_info.*


class AssetInfoFragment : BaseFragment<AssetInfoPresenter>(), AssetInfoContract.View {

    lateinit var pageAdapter: AssetInfoPageAdapter

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AssetInfoPresenter(this, AssetInfoRepository())
    }

    override fun getAssetId(): Int  = AssetInfoFragmentArgs.fromBundle(requireArguments()).id
    override fun getToolbarTitle(): String = AssetInfoFragmentArgs.fromBundle(requireArguments()).name
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_asset_info
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        toolbarLayout.hasStatus = true

        pageAdapter = AssetInfoPageAdapter(getAssetId(), AppActivity.self.supportFragmentManager, 2)
        pageAdapter.notifyDataSetChanged()

        tabLayout.addTab(tabLayout.newTab().setText(getText(R.string.balance)))
        tabLayout.addTab(tabLayout.newTab().setText(getText(R.string.asset_info)))

        viewPager.isSaveFromParentEnabled = false
        viewPager.adapter = pageAdapter

        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun init() {
        val asset = presenter?.getAsset()

    }


    override fun configAsset(asset: Asset?) {

    }

    override fun onStop() {
        super.onStop()
    }
}