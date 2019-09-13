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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod

/**
 * Created by vain onnellinen on 3/5/19.
 */
class EditAddressPresenter(currentView: EditAddressContract.View, currentRepository: EditAddressContract.Repository, private val state: EditAddressState)
    : BasePresenter<EditAddressContract.View, EditAddressContract.Repository>(currentView, currentRepository),
        EditAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        state.address = view?.getAddress()
        state.chosenPeriod = if (state.address!!.duration == 0L) ExpirePeriod.NEVER else ExpirePeriod.DAY
        if (state.tempComment.isBlank()) {
            state.tempComment = state.address?.label ?: ""
        }

        view?.init(state.address ?: return)

        val currentTags = repository.getAddressTags(state.address!!.walletID)

        state.tempTags = currentTags
        state.currentTags = currentTags

        view?.setTags(currentTags)
    }

    override fun onStart() {
        super.onStart()
        view?.setupTagAction(repository.getAllTags().isEmpty())

        if (state.shouldExpireNow) {
            view?.configExpireSpinnerTime(state.shouldExpireNow)
            view?.configSaveButton(shouldEnableButton())
        }
        else if (state.shouldActivateNow) {
            view?.configExpireSpinnerVisibility(state.shouldActivateNow)
            view?.configSaveButton(shouldEnableButton())
        }
    }

    override fun onSelectTags(tags: List<Tag>) {
        state.tempTags = tags
        view?.setTags(tags)
        view?.configSaveButton(shouldEnableButton())
    }

    override fun onTagActionPressed() {
        if (repository.getAllTags().isEmpty()) {
            view?.showCreateTagDialog()
        } else {
            view?.showTagsDialog(state.tempTags)
        }
    }

    override fun onCreateNewTagPressed() {
        view?.showAddNewCategory()
    }

    override fun onSwitchCheckedChange(isChecked: Boolean) {
        val isExpired = state.address?.isExpired ?: return

        if (isExpired) {
            state.shouldActivateNow = isChecked
            view?.configExpireSpinnerVisibility(isChecked)
            view?.configSaveButton(shouldEnableButton())
        } else {
            state.shouldExpireNow = isChecked
            view?.configExpireSpinnerTime(isChecked)
            view?.configSaveButton(shouldEnableButton())
        }
    }

    override fun onExpirePeriodChanged(period: ExpirePeriod) {
        state.chosenPeriod = period
        view?.configSaveButton(shouldEnableButton())
    }

    override fun onChangeComment(comment: String) {
        state.tempComment = comment.trim()
        view?.configSaveButton(shouldEnableButton())
    }

    private fun shouldEnableButton(): Boolean {
        val isExpired = state.address?.isExpired ?: return false

        val isExpireChanged =  if (isExpired) {
            state.shouldActivateNow
        } else {
            if (state.shouldExpireNow) {
                true
            } else {
                when {
                    state.address!!.duration == 0L && state.chosenPeriod == ExpirePeriod.DAY -> true
                    state.address!!.duration != 0L && state.chosenPeriod == ExpirePeriod.NEVER -> true
                    else -> false
                }
            }
        }

        val isCommentChanged = state.tempComment != state.address?.label ?: return false

        val isCategoryChanged = !state.tempTags.containsAll(state.currentTags) || state.tempTags.size != state.currentTags.size

        return isExpireChanged || isCommentChanged || isCategoryChanged
    }

    override fun onSavePressed() {
        val address = state.address ?: return
        address.label = state.tempComment.trim()

        repository.saveTagsForAddress(state.address!!.walletID, state.tempTags)

        if (!address.isContact) {
            if (address.isExpired) {
                if (state.shouldActivateNow) {
                    repository.saveAddressChanges(addr = address.walletID, name = address.label, makeActive = true, makeExpired = false, isNever = state.chosenPeriod == ExpirePeriod.NEVER)
                } else {
                    repository.updateAddress(address)
                }
            } else {
                if (state.shouldExpireNow) {
                    repository.saveAddressChanges(addr = address.walletID, name = address.label, makeActive = false, makeExpired = true, isNever = false)
                } else {
                    when {
                        state.chosenPeriod == ExpirePeriod.NEVER -> repository.saveAddressChanges(addr = address.walletID, name = address.label, makeActive = false, makeExpired = false, isNever = true)
                        state.chosenPeriod == ExpirePeriod.DAY -> repository.saveAddressChanges(addr = address.walletID, name = address.label, makeActive = true, makeExpired = false, isNever = false)
                    }
                }
            }
        } else {

            repository.saveAddress(address, false)
        }

        view?.finishScreen()
    }
}

