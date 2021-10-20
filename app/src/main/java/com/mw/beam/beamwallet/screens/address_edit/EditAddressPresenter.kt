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

import android.view.Menu
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod

/**
 *  3/5/19.
 */
class EditAddressPresenter(currentView: EditAddressContract.View, currentRepository: EditAddressContract.Repository, val state: EditAddressState)
    : BasePresenter<EditAddressContract.View, EditAddressContract.Repository>(currentView, currentRepository),
        EditAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()

        if (state.address == null)
        {
            state.address = view?.getAddress()
            state.chosenPeriod = if (state.address!!.duration == 0L) ExpirePeriod.NEVER else ExpirePeriod.EXTEND
            state.tempComment = state.address?.label ?: ""
        }

        view?.init(state.address ?: return)

        view?.configSaveButton(shouldEnableButton())
    }

    override fun onStart() {
        super.onStart()

        if (state.shouldExpireNow) {
            view?.configExpireSpinnerTime(state.shouldExpireNow)
            view?.configSaveButton(shouldEnableButton())
        }
        else if (state.shouldActivateNow) {
            view?.configSaveButton(shouldEnableButton())
        }
    }

    override fun onMenuCreate(menu: Menu?) {
        view?.configMenuItems(menu, state.address ?: return)
    }

    override fun onDeleteAddress() {
        if (state.getTransactions().isNotEmpty()) {
            view?.showDeleteAddressDialog(true)
        }
        else {
            view?.showDeleteAddressDialog(false)

            //  onConfirmDeleteAddress(false)
        }
    }

    override fun onConfirmDeleteAddress(withTransactions: Boolean) {
        state.address?.let {
            view?.showDeleteSnackBar(it)
            repository.deleteAddress(it, if (withTransactions) state.getTransactions() else listOf())
            view?.onAddressDeleted()
        }
    }

    override fun onSwitchCheckedChange(isChecked: Boolean) {
        val isExpired = state.address?.isExpired ?: return

        if (state.shouldExpireNow)
        {
            state.shouldActivateNow = true
            state.shouldExpireNow = false
            view?.configExpireSpinnerTime(false)
            view?.configSaveButton(shouldEnableButton())
        }
        else if (state.shouldActivateNow)
        {
            state.shouldActivateNow = false
            state.shouldExpireNow = true
            view?.configExpireSpinnerTime(true)
            view?.configSaveButton(shouldEnableButton())
        }
        else if (isExpired) {
            state.shouldActivateNow = true
            view?.configExpireSpinnerTime(isChecked)
            view?.configSaveButton(shouldEnableButton())
        } else {
            state.shouldExpireNow = true
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
                    state.address!!.duration == 0L && state.chosenPeriod == ExpirePeriod.EXTEND -> true
                    state.address!!.duration != 0L && state.chosenPeriod == ExpirePeriod.NEVER -> true
                    else -> false
                }
            }
        }

        val isCommentChanged = state.tempComment != state.address?.label ?: return false

        return isExpireChanged || isCommentChanged
    }

    override fun onSavePressed() {
        val address = state.address ?: return
        address.label = state.tempComment.trim()

        if (!address.isContact) {
            repository.saveAddressChanges(addr = address.id, name = address.label,
                makeActive = state.shouldActivateNow,
                makeExpired = state.shouldExpireNow,
                isExtend = state.shouldExtend)
        }
        else {
            repository.saveAddress(address, false)
        }

        view?.finishScreen()
    }
}

