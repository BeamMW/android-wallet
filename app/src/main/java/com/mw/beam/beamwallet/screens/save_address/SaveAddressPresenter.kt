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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.TrashManager

class SaveAddressPresenter(view: SaveAddressContract.View?, repository: SaveAddressContract.Repository, private val state: SaveAddressState)
    : BasePresenter<SaveAddressContract.View, SaveAddressContract.Repository>(view, repository), SaveAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.address = getAddress()

            init(state.address)
        }
    }

    override fun onSavePressed() {
        view?.apply {
            var identity = ""
            val address = state.address
            var id = state.address

            if(AppManager.instance.wallet?.isToken(address) == true)
            {
                val params = AppManager.instance.wallet?.getTransactionParameters(address, false)
                identity = params?.identity ?: ""
                id = params?.address ?: state.address
            }

            AppManager.instance.removeIgnoredAddress(address)

            val dto = WalletAddressDTO(id, getName(), "", System.currentTimeMillis(), 0, 0, identity, address)
            AppManager.lastSendSavedContact = dto
            AppManager.instance.wallet?.saveAddress(dto, false)

            close()
        }
    }

    override fun onCancelPressed() {
        onSavePressed()

        AppManager.instance.setIgnoreAddress(state.address)
        AppManager.instance.wallet?.getAddresses(false)
    }

}