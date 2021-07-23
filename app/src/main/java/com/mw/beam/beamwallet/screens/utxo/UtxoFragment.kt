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

package com.mw.beam.beamwallet.screens.utxo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.ExchangeManager
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.UtxoStatus

import kotlinx.android.synthetic.main.fragment_utxo.*

/**
 *  10/2/18.
 */
class UtxoFragment : BaseFragment<UtxoPresenter>(), UtxoContract.View {
    private lateinit var pagerAdapter: UtxosPagerAdapter

    override fun onControllerGetContentLayoutId() = R.layout.fragment_utxo
    override fun getToolbarTitle(): String = getString(R.string.utxo)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    override fun init() {
        val context = context ?: return

        pagerAdapter = UtxosPagerAdapter(context, object : UtxosAdapter.OnItemClickListener {
            override fun onItemClick(item: Utxo) {
                presenter?.onUtxoPressed(item)
            }
        })

        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onCreateOptionsMenu(menu, inflater)
    }


    override fun createOptionsMenu(menu: Menu?, inflater: MenuInflater?, isEnablePrivacyMode: Boolean) {
        inflater?.inflate(R.menu.privacy_menu, menu)
        val menuItem = menu?.findItem(R.id.privacy_mode)
        menuItem?.setOnMenuItemClickListener {
            presenter?.onChangePrivacyModePressed()
            false
        }

        menuItem?.setIcon(if (isEnablePrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.cancel), { presenter?.onCancelDialog() })
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        ExchangeManager.instance.isPrivacyMode = isEnable

        activity?.invalidateOptionsMenu()
        setVisibility()
    }

    override fun showUtxoDetails(utxo: Utxo) {
        findNavController().navigate(UtxoFragmentDirections.actionUtxoFragmentToUtxoDetailsFragment(utxo))
    }

    override fun updateUtxos(utxos: List<Utxo>) {
        pagerAdapter.setData(Tab.SPENT, utxos.filter { it.status == UtxoStatus.Spent})
        pagerAdapter.setData(Tab.AVAILABLE, utxos.filter { it.status == UtxoStatus.Available})
        pagerAdapter.setData(Tab.PROGRESS, utxos.filter { it.status == UtxoStatus.Maturing || it.status == UtxoStatus.Incoming || it.status == UtxoStatus.Outgoing})
        pagerAdapter.setData(Tab.UNAVAILABLE, utxos.filter { it.status == UtxoStatus.Unavailable})
        setVisibility()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return UtxoPresenter(this, UtxoRepository(), UtxoState())
    }

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    @SuppressLint("SetTextI18n")
    private fun setVisibility() {
        if (presenter?.repository?.isPrivacyModeEnabled() == false && utxoPrivacyMessage!=null) {

            utxoPrivacyMessage.setPadding(0,0,0,170)

            privacyLabel.text = context?.getString(R.string.empty_utxo_list)
            privacyLabel.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_utxo_empty_state, 0, 0);

            if (presenter?.utxosCount == 0) {
                utxoPrivacyMessage.visibility = View.VISIBLE
                pager.visibility = View.GONE
                tabLayout.visibility = View.INVISIBLE
            } else {
                utxoPrivacyMessage.visibility = View.GONE
                pager.visibility = View.VISIBLE
                tabLayout.visibility = View.VISIBLE
            }
        }
        else if (utxoPrivacyMessage!=null){
            utxoPrivacyMessage.setPadding(0,0,0,50)

            privacyLabel.text = getString(R.string.utxo_security_message) + "\n" + getString(R.string.utxo_turn_off_see_utxo)
            privacyLabel.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_eye_crossed_big, 0, 0);

            utxoPrivacyMessage.visibility = View.VISIBLE
            pager.visibility = View.GONE
            tabLayout.visibility = View.GONE
        }
    }
}
