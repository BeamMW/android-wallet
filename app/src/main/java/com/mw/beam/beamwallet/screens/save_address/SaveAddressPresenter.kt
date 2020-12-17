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
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TrashManager

class SaveAddressPresenter(view: SaveAddressContract.View?, repository: SaveAddressContract.Repository, private val state: SaveAddressState)
    : BasePresenter<SaveAddressContract.View, SaveAddressContract.Repository>(view, repository), SaveAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.address = getAddress()
            state.tags = repository.getAddressTags(state.address)

            init(state.address)
        }
    }

    override fun onStart() {
        super.onStart()
        view?.setupTagAction(repository.getAllTags().isEmpty())
    }

    override fun onSavePressed() {
        view?.apply {
            var saved = state.address
            var identity = ""
            var token = ""
            if (AppManager.instance.wallet?.isToken(state.address) == true) {
                var params = AppManager.instance.wallet?.getTransactionParameters(saved, false)
                saved = params!!.address
                identity = params!!.identity
                token = saved
            }
            val address = WalletAddress(WalletAddressDTO(saved,
                    getName(), "",
                    System.currentTimeMillis(), 0, 0,
                    identity,
                    token))
            if(state.tags.count() > 0) {
                repository.saveTagsForAddress(saved, state.tags)
            }
            repository.saveAddress(address, false)
            close()
        }
    }

    override fun onCreateNewTagPressed() {
        view?.showAddNewCategory()
    }

    override fun onSelectTags(tags: List<Tag>) {
        state.tags = tags
        view?.setTags(tags)
    }

    override fun onTagActionPressed() {
        if (repository.getAllTags().isEmpty()) {
            view?.showCreateTagDialog()
        } else {
            view?.showTagsDialog(state.tags)
        }
    }

    override fun onCancelPressed() {
        val address = WalletAddress(WalletAddressDTO(state.address,"deleted","",0L,0L,0L, "", ""))

        TrashManager.add(state.address, address)

        AppManager.instance.wallet?.deleteAddress(state.address)

        view?.close()
    }

}