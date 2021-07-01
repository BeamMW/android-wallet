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

package com.mw.beam.beamwallet.screens.asset_detail

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.*
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
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.screens.wallet.AssetsAdapter
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter

import kotlinx.android.synthetic.main.fragment_asset_detail.*


class AssetDetailFragment : BaseFragment<AssetDetailPresenter>(), AssetDetailContract.View {
    private lateinit var assetsAdapter: AssetsAdapter
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AssetDetailPresenter(this, AssetDetailRepository())
    }

    override fun getToolbarTitle(): String = AssetDetailFragmentArgs.fromBundle(requireArguments()).name
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_asset_detail
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

        btnShowAll.setOnClickListener {
            findNavController().navigate(AssetDetailFragmentDirections.actionAssetDetailFragmentToTransactionsFragment(getAssetId()))
        }
    }

    override fun init() {
        val asset = presenter?.getAsset()
        val list = if (asset == null) {
            arrayListOf()
        }
        else {
            arrayListOf(asset)
        }

        assetsAdapter = AssetsAdapter(requireContext(),list) {}
        assetsList.layoutManager = LinearLayoutManager(context)
        assetsList.adapter = assetsAdapter

        val transactions = mutableListOf<TxDescription>()
        presenter?.getTransactions()?.let { transactions.addAll(it) }

        transactionsAdapter = TransactionsAdapter(requireContext(), null, transactions,
                TransactionsAdapter.Mode.SHORT) {
            findNavController().navigate(AssetDetailFragmentDirections.actionAssetDetailFragmentToTransactionDetailsFragment(it.id))
        }
        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = transactionsAdapter
    }

    override fun getAssetId(): Int {
        return AssetDetailFragmentArgs.fromBundle(requireArguments()).id
    }

    override fun configAsset(asset: Asset?) {
        val list = if (asset == null) {
            arrayListOf()
        }
        else {
            arrayListOf(asset)
        }
        assetsAdapter.reloadData(list)
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        transactionsAdapter.data = transactions
        transactionsAdapter.notifyDataSetChanged()

        if (transactions.isEmpty()) {
            transactionsHeader.visibility = View.GONE
            emptyTransactionsListMessage.visibility = View.VISIBLE
            transactionsList.visibility = View.GONE
        }
        else {
            transactionsHeader.visibility = View.VISIBLE
            emptyTransactionsListMessage.visibility = View.GONE
            transactionsList.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.asset_menu, menu)

        val menuInfo = menu.findItem(R.id.info_menu)
        menuInfo?.setOnMenuItemClickListener {
            openInfo()
            false
        }
        menuInfo?.setIcon(R.drawable.ic_info_button_asset)

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
        transactionsAdapter.notifyDataSetChanged()
    }

    private fun openInfo() {
        findNavController().navigate(AssetDetailFragmentDirections.actionAssetDetailFragmentToAssetInfoFragment(getAssetId(), AssetDetailFragmentArgs.fromBundle(requireArguments()).name))
    }
}