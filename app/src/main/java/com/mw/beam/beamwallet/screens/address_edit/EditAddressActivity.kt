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

package com.mw.beam.beamwallet.screens.address_edit

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress

/**
 * Created by vain onnellinen on 3/5/19.
 */
class EditAddressActivity : BaseActivity<EditAddressPresenter>(), EditAddressContract.View {
    private lateinit var presenter: EditAddressPresenter

    companion object {
        const val EXTRA_ADDRESS_FOR_EDIT = "EXTRA_ADDRESS_FOR_EDIT"
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_edit_address
    override fun getToolbarTitle(): String? = getString(R.string.edit_address_title)
    override fun getAddress(): WalletAddress = intent.getParcelableExtra(EXTRA_ADDRESS_FOR_EDIT)

    override fun init(address: WalletAddress) {

    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = EditAddressPresenter(this, EditAddressRepository(), EditAddressState())
        return presenter
    }
}

