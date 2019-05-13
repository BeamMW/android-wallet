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

package com.mw.beam.beamwallet.screens.addresses

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import kotlinx.android.synthetic.main.fragment_addresses.*

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesFragment : BaseFragment<AddressesPresenter>(), AddressesContract.View {
    private lateinit var presenter: AddressesPresenter
    private lateinit var pagerAdapter: AddressesPagerAdapter

    companion object {
        fun newInstance() = AddressesFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = AddressesFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_addresses
    override fun getToolbarTitle(): String? = getString(R.string.addresses_title)

    override fun init() {
        val context = context ?: return

        pagerAdapter = AddressesPagerAdapter(context, object : AddressesAdapter.OnItemClickListener {
            override fun onItemClick(item: WalletAddress) {
                presenter.onAddressPressed(item)
            }
        }, presenter::onSearchCategoryForAddress)

        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)
    }

    override fun showAddressDetails(address: WalletAddress) = (activity as AddressDetailsHandler).onShowAddressDetails(address)

    override fun updateAddresses(tab: Tab, addresses: List<WalletAddress>) {
        pagerAdapter.setData(tab, addresses)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = AddressesPresenter(this, AddressesRepository())
        return presenter
    }

    interface AddressDetailsHandler {
        fun onShowAddressDetails(item: WalletAddress)
    }
}
