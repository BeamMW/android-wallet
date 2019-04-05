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

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.SystemState
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import kotlinx.android.synthetic.main.fragment_utxo.*

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoFragment : BaseFragment<UtxoPresenter>(), UtxoContract.View {
    private lateinit var presenter: UtxoPresenter
    private lateinit var pagerAdapter: UtxosPagerAdapter

    companion object {
        fun newInstance() = UtxoFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = UtxoFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_utxo
    override fun getToolbarTitle(): String? = getString(R.string.utxo_title)

    override fun init() {
        val context = context ?: return

        pagerAdapter = UtxosPagerAdapter(context, object : UtxosAdapter.OnItemClickListener {
            override fun onItemClick(item: Utxo) {
                presenter.onUtxoPressed(item)
            }
        })

        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)
    }

    override fun showUtxoDetails(utxo: Utxo, relatedTransactions: ArrayList<TxDescription>) = (activity as UtxoDetailsHandler).onShowUtxoDetails(utxo, relatedTransactions)

    override fun updateUtxos(utxos: List<Utxo>) {
        pagerAdapter.setData(Tab.ACTIVE, utxos.filter { it.status == UtxoStatus.Available || it.status == UtxoStatus.Maturing })
        pagerAdapter.setData(Tab.ALL, utxos)
    }

    override fun updateBlockchainInfo(systemState: SystemState) {
        blockchainHeightValue.text = systemState.height.toString()
        blockchainHashValue.text = systemState.hash
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = UtxoPresenter(this, UtxoRepository(), UtxoState())
        return presenter
    }

    interface UtxoDetailsHandler {
        fun onShowUtxoDetails(item: Utxo, relatedTransactions: ArrayList<TxDescription>)
    }
}
