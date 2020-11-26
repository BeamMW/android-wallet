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

package com.mw.beam.beamwallet.screens.unlink

import android.annotation.SuppressLint
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToCurrencyGrothString
import com.mw.beam.beamwallet.core.helpers.convertToCurrencyString
import com.mw.beam.beamwallet.screens.confirm.PasswordConfirmDialog

import kotlinx.android.synthetic.main.fragment_unlink_confirmation.*

class UnlinkConfirmationFragment : BaseFragment<UnlinkConfirmationPresenter>(), UnlinkConfirmationContract.View {

    private val args: UnlinkConfirmationFragmentArgs by lazy {
        UnlinkConfirmationFragmentArgs.fromBundle(arguments!!)
    }

    override fun getToolbarTitle(): String? = getString(R.string.confirmation)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_unlink_confirmation

    override fun getAmount(): Long = args.sendAmount
    override fun getFee(): Long = args.fee

    @SuppressLint("SetTextI18n")
    override fun init(amount: Double, fee: Long, remaining: Long) {
        amountToSend.text = "${amount.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondAvailableSum.text = amount.convertToCurrencyString()
        secondFeeleSum.text = fee.convertToCurrencyGrothString()

        totalRemaining.text = "${remaining.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondRemainingSum.text = remaining.convertToCurrencyString()

        this.fee.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, R.color.unlink_color)
    }

    override fun showConfirmDialog() {
       this.passwordDialog =  PasswordConfirmDialog.newInstance(PasswordConfirmDialog.Mode.SendBeam, {
            presenter?.onConfirmed()
        }, {

        })
        this.passwordDialog?.show(activity?.supportFragmentManager!!, PasswordConfirmDialog.getFragmentTag())
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter?.onSendPressed()
        }
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
    }

    @SuppressLint("SetTextI18n")
    override fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double) {
        totalUtxoTitle.visibility = View.VISIBLE
        totalUtxo.visibility = View.VISIBLE
        changeUtxoTitle.visibility = View.VISIBLE
        changeUtxo.visibility = View.VISIBLE

        totalUtxo.text = "${usedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        changeUtxo.text = "${changedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        secondUtxoSum.text = usedUtxo.convertToCurrencyString()
        secondChangeSum.text = changedUtxo.convertToCurrencyString()
    }

    override fun delaySend(amount: Long, fee: Long) {
       // (activity as? AppActivity)?.pendingSend(PendingSendInfo(token, comment, amount, fee, outgoingAddress))
    }


    override fun showWallet() {
        findNavController().popBackStack(R.id.walletFragment, false)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return UnlinkConfirmationPresenter(this, UnlinkConfirmationRepository(), UnlinkConfirmationState())
    }
}