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

package com.mw.beam.beamwallet.screens.assets_list

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.*
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.screens.max_privacy_details.MaxPrivacyDetailSort
import com.mw.beam.beamwallet.screens.wallet.AssetsAdapter
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*

import kotlinx.android.synthetic.main.fragment_assets_list.*

class AssetsListFragment : BaseFragment<AssetsListPresenter>(), AssetsListContract.View {
    private lateinit var assetsAdapter: AssetsAdapter

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AssetsListPresenter(this, AssetsListRepository())
    }
    override fun getToolbarTitle(): String = getString(R.string.assets)
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_assets_list
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

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        itemsswipetorefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        itemsswipetorefresh.setOnRefreshListener {
            AppManager.instance.reload()
            android.os.Handler().postDelayed({
                if (itemsswipetorefresh!=null) {
                    itemsswipetorefresh.isRefreshing = false
                }
            }, 1000)
        }

        btnNext.setOnClickListener {
            findNavController().navigate(AssetsListFragmentDirections.actionAssetsListFragmentToSendFragment())
        }
        btnReceive.setOnClickListener {
            findNavController().navigate(AssetsListFragmentDirections.actionAssetsListFragmentToReceiveFragment())
        }
    }


    override fun init() {
        assetsAdapter = AssetsAdapter(
                requireContext(), presenter?.getAssets() ?: arrayListOf()
        ) {
            presenter?.onAssetPressed(it)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = assetsAdapter
    }

    override fun showAssetDetails(asset: Asset) {
        findNavController().navigate(AssetsListFragmentDirections.actionAssetsListFragmentToAssetDetailFragment(asset.assetId, asset.unitName))
    }

    override fun configAssets(asset: List<Asset>) {
        assetsAdapter.reloadData(asset)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.assets_menu, menu)

        val menuItemFilter = menu.findItem(R.id.filter_menu)
        menuItemFilter?.setOnMenuItemClickListener {
            showSortDialog()
            false
        }
        menuItemFilter?.setIcon(R.drawable.ic_filter)

        val menuPrivacy = menu.findItem(R.id.privacy_mode)
        menuPrivacy?.setOnMenuItemClickListener {
            presenter?.onChangePrivacyModePressed()
            false
        }
        menuPrivacy?.setIcon(if (ExchangeManager.instance.isPrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate),
                { presenter?.onPrivacyModeActivated() },
                getString(R.string.common_security_mode_title), getString(R.string.cancel), { dismissAlert() })
    }

    override fun configPrivacyStatus() {
        activity?.invalidateOptionsMenu()
        assetsAdapter.notifyDataSetChanged()
    }

    private fun showSortDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_max_privacy_sort, null)

            val valuesArray = resources.getIntArray(R.array.assets_sort_values)

            valuesArray.forEach { type ->

                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = getTitleStringValue(type)
                    isChecked = type == presenter?.filter?.ordinal
                    setOnClickListener {
                        presenter?.onChangeFilter(AssetFilter.fromValue(type))
                        dialog?.dismiss()
                    }
                }

                view.radioGroupLockSettings.addView(button)
            }

            view.btnCancel.setOnClickListener {dialog?.dismiss() }
            dialog = AlertDialog.Builder(it).setView(view).show()
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun getTitleStringValue(value:Int):String {
        return when (value) {
            0 -> {
                requireContext().getString(R.string.usage_recent_old)
            }
            1 -> {
                requireContext().getString(R.string.usage_old_recent)
            }
            2 -> {
                requireContext().getString(R.string.amount_small_large)
            }
            3 -> {
                requireContext().getString(R.string.amount_large_small)
            }
            4 -> {
                requireContext().getString(R.string.usd_small_large)
            }
            else -> {
                requireContext().getString(R.string.usd_large_small)
            }
        }
    }
}