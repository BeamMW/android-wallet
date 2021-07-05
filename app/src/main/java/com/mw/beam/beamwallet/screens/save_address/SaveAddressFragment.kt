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

package com.mw.beam.beamwallet.screens.save_address


import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.trimAddress

import kotlinx.android.synthetic.main.fragment_save_address.*


class SaveAddressFragment: BaseFragment<SaveAddressPresenter>(), SaveAddressContract.View {
    override fun getAddress(): String = SaveAddressFragmentArgs.fromBundle(requireArguments()).address

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_save_address

    override fun init(address: String) {
        this.address.text = address.trimAddress()
    }

    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        btnCancel.setOnClickListener {
            presenter?.onCancelPressed()
        }

    }

    override fun getName(): String {
        return name.text?.toString() ?: ""
    }


    override fun clearListeners() {
        btnSave.setOnClickListener(null)
        btnCancel.setOnClickListener(null)
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SaveAddressPresenter(this, SaveAddressRepository(), SaveAddressState())
    }

    override fun getToolbarTitle(): String = getString(R.string.save_address)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (App.isDarkMode) {
            gradientView.setBackgroundResource(R.drawable.send_bg_dark)
        }
        else {
            gradientView.setBackgroundResource(R.drawable.send_bg)
        }
    }

    override fun getStatusBarColor(): Int {
        return if (App.isDarkMode) {
            ContextCompat.getColor(requireContext(), R.color.send_toolbar_color_dark)
        }
        else {
            ContextCompat.getColor(requireContext(), R.color.send_toolbar_color)
        }
    }
}