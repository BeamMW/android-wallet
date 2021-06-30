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

package com.mw.beam.beamwallet.screens.max_privacy_details

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App

import com.mw.beam.beamwallet.core.entities.Utxo
import kotlinx.android.synthetic.main.dialog_lock_screen_settings.view.*
import kotlinx.android.synthetic.main.fragment_max_privacy_details.*


enum class MaxPrivacyDetailSort {
    time_ear, time_latest, amount_small, amount_large;

    companion object {
        private val map: HashMap<Int, MaxPrivacyDetailSort> = HashMap()

        init {
            MaxPrivacyDetailSort.values().forEach {
                map[it.ordinal] = it
            }
        }

        fun fromValue(type: Int): MaxPrivacyDetailSort {
            return map[type] ?: MaxPrivacyDetailSort.time_ear
        }
    }
}

class MaxPrivacyDetailFragment: BaseFragment<MaxPrivacyDetailPresenter>(), MaxPrivacyDetailContract.View {
    private lateinit var adapter: MaxPrivacyDetailAdapter

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_max_privacy_details

    override fun getToolbarTitle(): String = getString(R.string.max_privacy)
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }

    private var sort = MaxPrivacyDetailSort.time_ear

    override fun init(utxos: List<Utxo>) {
        adapter = MaxPrivacyDetailAdapter(utxos) {
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        toolbarLayout.hasStatus = true
        toolbarLayout.centerTitle = true
    }

    override fun changeFilter(utxos: List<Utxo>) {
        adapter.reload(utxos)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return MaxPrivacyDetailPresenter(this, MaxPrivacyDetailRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.filter_menu, menu)
        val menuItem = menu.findItem(R.id.filter_menu)
        menuItem?.setOnMenuItemClickListener {
            showSortDialog()
            false
        }

        menuItem?.setIcon(R.drawable.ic_filter)
    }

    private fun showSortDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_max_privacy_sort, null)

            val valuesArray = resources.getIntArray(R.array.max_privacy_sort_values)

            valuesArray.forEach { type ->

                val button = LayoutInflater.from(it).inflate(R.layout.lock_radio_button, view.radioGroupLockSettings, false)

                (button as RadioButton).apply {
                    text = getMaxPrivacyStringValue(type)
                    isChecked = type == sort.ordinal
                    setOnClickListener {
                        sort = MaxPrivacyDetailSort.fromValue(type)
                        presenter?.onSelectFilter(sort)
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

    private fun getMaxPrivacyStringValue(value: Int): String {
        return when (value) {
            0 -> {
                requireContext().getString(R.string.time_ear)
            }
            1 -> {
                requireContext().getString(R.string.time_latest)
            }
            2 -> {
                requireContext().getString(R.string.amount_small)
            }
            else -> {
                requireContext().getString(R.string.amount_large)
            }
        }
    }
}