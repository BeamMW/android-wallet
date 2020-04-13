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

package com.mw.beam.beamwallet.screens.currency

import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.LocaleHelper
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_currency.*
import java.util.Locale
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.core.entities.Currency
import com.mw.beam.beamwallet.core.entities.ExchangeRate


class CurrencyFragment: BaseFragment<CurrencyPresenter>(), CurrencyContract.View {
    private lateinit var adapter: CurrencyAdapter

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_currency

    override fun getToolbarTitle(): String? = getString(R.string.second_currency)
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)
    }

    override fun init(currencies: List<ExchangeRate>, currency: Currency) {
        adapter = CurrencyAdapter(currencies) {
            presenter?.onSelectCurrency(it)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter.currency(currency)
    }

    override fun changeCurrency(currency: Currency) {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return CurrencyPresenter(this, CurrencyRepository())
    }
}