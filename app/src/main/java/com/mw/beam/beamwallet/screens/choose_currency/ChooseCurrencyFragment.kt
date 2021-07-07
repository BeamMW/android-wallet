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

package com.mw.beam.beamwallet.screens.choose_currency

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate
import kotlinx.android.synthetic.main.fragment_choose_currency.*


class ChooseCurrencyFragment: BaseFragment<ChooseCurrencyPresenter>(), ChooseCurrencyContract.View {
    private lateinit var adapter: ChooseCurrencyAdapter

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_choose_currency

    override fun getToolbarTitle(): String = getString(R.string.choose_currency)
    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(requireContext(), R.color.sent_color)
    }

    private val args: ChooseCurrencyFragmentArgs by lazy {
        ChooseCurrencyFragmentArgs.fromBundle(requireArguments())
    }

    override fun init(assets: List<Asset>) {
        adapter = ChooseCurrencyAdapter(assets) {
            presenter?.onSelectAsset(it)
        }
        adapter.currency(args.selectedCurrency)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun changeAsset(assetId: Int) {
        setFragmentResult("CURRENCY_FRAGMENT", bundleOf("currency" to assetId))
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ChooseCurrencyPresenter(this, ChooseCurrencyRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarLayout.hasStatus = true
    }
}