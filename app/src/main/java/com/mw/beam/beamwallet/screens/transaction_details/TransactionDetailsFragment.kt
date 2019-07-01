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

package com.mw.beam.beamwallet.screens.transaction_details

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.view.*
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.views.addDoubleDots
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import kotlinx.android.synthetic.main.item_transaction.*
import kotlinx.android.synthetic.main.item_transaction_utxo.view.*

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsFragment : BaseFragment<TransactionDetailsPresenter>(), TransactionDetailsContract.View {
    private var moreMenu: Menu? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_transaction_details
    override fun getToolbarTitle(): String? = getString(R.string.transaction_details)
    override fun getTransactionId(): String = TransactionDetailsFragmentArgs.fromBundle(arguments!!).txId

    override fun init(txDescription: TxDescription, isEnablePrivacyMode: Boolean) {
        configTransactionDetails(txDescription, isEnablePrivacyMode)
        configGeneralTransactionInfo(txDescription)
        setHasOptionsMenu(true)
        activity?.invalidateOptionsMenu()
        moreMenu?.close()

        transactionFeeTitle.addDoubleDots()
        commentTitle.addDoubleDots()
        transactionIdTitle.addDoubleDots()
        kernelTitle.addDoubleDots()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onMenuCreate(menu, inflater)
    }

    override fun configMenuItems(menu: Menu?, inflater: MenuInflater, txStatus: TxStatus) {
        if (TxStatus.InProgress == txStatus
                || TxStatus.Pending == txStatus
                || TxStatus.Failed == txStatus
                || TxStatus.Completed == txStatus
                || TxStatus.Cancelled == txStatus) {
            inflater.inflate(R.menu.transaction_menu, menu)
            moreMenu = menu
            menu?.findItem(R.id.cancel)?.isVisible = TxStatus.InProgress == txStatus || TxStatus.Pending == txStatus
            menu?.findItem(R.id.delete)?.isVisible = TxStatus.Failed == txStatus || TxStatus.Completed == txStatus || TxStatus.Cancelled == txStatus
            menu?.findItem(R.id.repeat)?.isVisible = TxStatus.Failed == txStatus || TxStatus.Cancelled == txStatus
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // R.id.save -> {  }
            R.id.repeat -> presenter?.onRepeatTransaction()
            R.id.cancel -> presenter?.onCancelTransaction()
            R.id.delete -> presenter?.onDeleteTransaction()
        }

        return true
    }

    @SuppressLint("InflateParams")
    override fun updateUtxos(utxoInfoList: List<UtxoInfoItem>, isEnablePrivacyMode: Boolean) {
        transactionUtxoContainer.visibility = if (utxoInfoList.isEmpty() || isEnablePrivacyMode) View.GONE else View.VISIBLE
        transactionUtxoList.removeAllViews()

        utxoInfoList.forEach { utxo ->
            val utxoView = LayoutInflater.from(context).inflate(R.layout.item_transaction_utxo, null)

            val drawableId = when(utxo.type) {
                UtxoType.Send -> R.drawable.ic_history_sent
                UtxoType.Receive -> R.drawable.ic_history_received
                UtxoType.Exchange -> R.drawable.menu_utxo
            }

            utxoView.utxoIcon.setImageDrawable(context?.getDrawable(drawableId))
            utxoView.utxoAmount.text = utxo.amount.convertToBeamString()

            transactionUtxoList.addView(utxoView)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun configTransactionDetails(txDescription: TxDescription, isEnablePrivacyMode: Boolean) {
        val messageTitle = when (txDescription.sender) {
            TxSender.RECEIVED -> getString(R.string.receive)
            TxSender.SENT -> getString(R.string.send)
        }

        message.text =  "$messageTitle ${getString(R.string.currency_beam)}"

        icon.setImageResource(R.drawable.ic_beam)
        date.text = CalendarUtils.fromTimestamp(txDescription.modifyTime)
        currency.setImageDrawable(txDescription.currencyImage)

        sum.text = txDescription.amount.convertToBeamWithSign(txDescription.sender.value)
        sum.setTextColor(txDescription.amountColor)

        status.setTextColor(txDescription.statusColor)
        status.text = txDescription.statusString

        sum.visibility = if (isEnablePrivacyMode) View.GONE else View.VISIBLE
    }

    override fun configCategoryAddresses(senderCategory: Category?, receiverCategory: Category?) {
        startAddressCategory.visibility = if (senderCategory == null) View.GONE else View.VISIBLE
        senderCategory?.let {
            startAddressCategory.text = it.name
            startAddressCategory.setTextColor(resources.getColor(it.color.getAndroidColorId(), context?.theme))
        }

        endAddressCategory.visibility = if (receiverCategory == null) View.GONE else View.VISIBLE
        receiverCategory?.let {
            endAddressCategory.text = it.name
            endAddressCategory.setTextColor(resources.getColor(it.color.getAndroidColorId(), context?.theme))
        }
    }

    override fun updatePaymentProof(paymentProof: PaymentProof) {
        if (paymentProofContainer.visibility == View.VISIBLE) return

        TransitionManager.beginDelayedTransition(transactionDetailsMainContainer)
        paymentProofContainer.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        if (txDescription.sender.value) {
            startAddress.text = txDescription.myId
            endAddress.text = txDescription.peerId

            if (txDescription.selfTx) {
                startAddressTitle.text = "${getString(R.string.my_sending_address)}:"
                endAddressTitle.text = "${getString(R.string.my_receiving_address)}:"
            } else {
                startAddressTitle.text = "${getString(R.string.my_address)}:"
                endAddressTitle.text = "${getString(R.string.contact)}:"
            }
        } else {
            startAddressTitle.text = "${getString(R.string.contact)}:"
            endAddressTitle.text = "${getString(R.string.my_address)}:"
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        transactionFee.text = txDescription.fee.convertToBeamString()
        transactionId.text = txDescription.id
        kernel.text = txDescription.kernelId

        val externalLinkVisibility = if (isValidKernelId(txDescription.kernelId)) View.VISIBLE else View.GONE
        btnOpenInBlockExplorer.visibility = externalLinkVisibility
        externalLinkIcon.visibility = externalLinkVisibility

        if (txDescription.message.isNotEmpty()) {
            comment.text = txDescription.message
            commentTitle.visibility = View.VISIBLE
            comment.visibility = View.VISIBLE
        }
    }

    override fun showOpenLinkAlert() {
        showAlert(
                getString(R.string.common_external_link_dialog_message),
                getString(R.string.open),
                { presenter?.onOpenLinkPressed() },
                getString(R.string.common_external_link_dialog_title),
                getString(R.string.cancel)
        )
    }

    private fun isValidKernelId(kernelId: String) = try {
        kernelId.toInt() != 0
    } catch (e: Exception) {
        true
    }

    override fun addListeners() {
        btnPaymentProofDetails.setOnClickListener {
            presenter?.onShowPaymentProof()
        }

        btnPaymentProofCopy.setOnClickListener {
            presenter?.onCopyPaymentProof()
        }

        btnOpenInBlockExplorer.setOnClickListener {
            presenter?.onOpenInBlockExplorerPressed()
        }
    }

    override fun showSendFragment(address: String, amount: Long) {
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToSendFragment(address, amount))
    }

    override fun showReceiveFragment(amount: Long, walletAddress: WalletAddress?) {
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToReceiveFragment(amount, walletAddress))
    }

    override fun showPaymentProof(paymentProof: PaymentProof) {
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToPaymentProofDetailsFragment(paymentProof))
    }

    override fun showCopiedAlert() {
        showSnackBar(getString(R.string.copied))
    }

    override fun clearListeners() {
        btnPaymentProofDetails.setOnClickListener(null)
        btnPaymentProofCopy.setOnClickListener(null)
        btnOpenInBlockExplorer.setOnClickListener(null)
    }

    override fun finishScreen() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return TransactionDetailsPresenter(this, TransactionDetailsRepository(), TransactionDetailsState())
    }
}
