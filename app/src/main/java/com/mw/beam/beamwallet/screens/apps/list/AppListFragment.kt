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

package com.mw.beam.beamwallet.screens.apps.list

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.DAOManager
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.settings.SettingsFragmentMode
import com.mw.beam.beamwallet.screens.wallet.WalletFragmentDirections

import kotlinx.android.synthetic.main.fragment_app_list.*
import kotlinx.android.synthetic.main.toolbar.*

class AppListFragment : BaseFragment<AppListPresenter>(), AppListContract.View {

    private lateinit var adapter: AppsListAdapter

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AppListPresenter(this, AppListRepository())
    }

    override fun getToolbarTitle(): String = getString(R.string.dAppStore)
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_app_list
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showWalletFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.hasStatus = true

        onBackPressedCallback.isEnabled = true
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        adapter = AppsListAdapter(requireContext(), DAOManager.apps) { app ->
            if (app.support == true) {
                findNavController().navigate(AppListFragmentDirections.actionAppListFragmentToAppDetailFragment(app))
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun init() {

    }

}