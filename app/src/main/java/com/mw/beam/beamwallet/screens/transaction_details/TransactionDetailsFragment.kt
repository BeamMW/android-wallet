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
import android.content.Intent
import android.graphics.Bitmap
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.*
import androidx.navigation.fragment.findNavController
import android.os.Handler
import android.view.MenuInflater
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableStringBuilder
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.tabs.TabLayout

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.*
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.CalendarUtils

import java.io.File

import kotlinx.android.synthetic.main.fragment_transaction_details.*


class TransactionDetailsFragment : BaseFragment<TransactionDetailsPresenter>(), TransactionDetailsContract.View {
    private var moreMenu: Menu? = null
    private var share_transaction_details: ShareTransactionDetailsView? = null

    private var oldTransaction: TxDescription? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_transaction_details
    override fun getToolbarTitle(): String = getString(R.string.transaction_details)
    override fun getTransactionId(): String = TransactionDetailsFragmentArgs.fromBundle(requireArguments()).txId
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
}

    var txId:String = ""

    override fun init(txDescription: TxDescription, isEnablePrivacyMode: Boolean) {
        txId = txDescription.id

        if (oldTransaction?.status != txDescription.status || dateLabel.text.isNullOrEmpty()) {
            oldTransaction = txDescription

            configGeneralTransactionInfo(txDescription)

            setHasOptionsMenu(true)

            activity?.invalidateOptionsMenu()

            moreMenu?.close()

            toolbarLayout.hasStatus = true
        }

        if (txDescription.hasPaymentProof()) {
            tabLayout.visibility = View.VISIBLE
        }
        else {
            val params = mainScroll.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = ScreenHelper.dpToPx(requireContext(), 20)
            mainScroll.layoutParams = params

            tabLayout.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.general)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.payment_proof)))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    mainScroll.visibility = View.VISIBLE
                    proofScroll.visibility = View.GONE
                }
                else {
                    mainScroll.visibility = View.GONE
                    proofScroll.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onMenuCreate(menu, inflater)
    }

    override fun configMenuItems(menu: Menu?, inflater: MenuInflater, transaction: TxDescription?) {
        inflater.inflate(R.menu.transaction_menu, menu)

        if (transaction != null) {
            val txStatus = transaction.status
            val isSend = transaction.sender == TxSender.SENT

            moreMenu = menu

            if (transaction.selfTx || transaction.isDapps == true) {
                menu?.findItem(R.id.saveContact)?.isVisible = false
            }
            else {
                val contact = AppManager.instance.getAddress(transaction.peerId)
                menu?.findItem(R.id.saveContact)?.isVisible = contact == null
            }

            menu?.findItem(R.id.dapp)?.isVisible = transaction.isDapps == true
            menu?.findItem(R.id.cancel)?.isVisible = TxStatus.InProgress == txStatus || TxStatus.Pending == txStatus
            menu?.findItem(R.id.delete)?.isVisible = TxStatus.Failed == txStatus || TxStatus.Completed == txStatus || TxStatus.Cancelled == txStatus
            menu?.findItem(R.id.repeat)?.isVisible = isSend && TxStatus.InProgress != txStatus && txStatus != TxStatus.Registered
                    && transaction.isMaxPrivacy == false && transaction.isPublicOffline == false && transaction.isMaxPrivacy == false
                    && transaction.isDapps == false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.repeat -> presenter?.onRepeatTransaction()
            R.id.cancel -> presenter?.onCancelTransaction()
            R.id.delete -> presenter?.onDeleteTransaction()
            R.id.share -> {
                share_transaction_details = ShareTransactionDetailsView(requireContext())

                if (share_transaction_details != null) {
                    share_transaction_details?.configGeneralTransactionInfo(presenter?.state?.txDescription)
                    share_transaction_details?.layoutParams = ViewGroup.LayoutParams(ScreenHelper.dpToPx(context, 420),
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    mainConstraintLayout.addView(share_transaction_details, 0)
                    share_transaction_details?.alpha = 0f
                }

                Handler().postDelayed({
                    presenter?.onSharePressed()
                }, 100)
            }
            R.id.saveContact -> presenter?.onSaveContact()
            R.id.copy -> presenter?.onCopyDetailsPressed()
            R.id.dapp -> {
                val app = DAOManager.apps.firstOrNull {
                    it.name == oldTransaction?.appName
                }
                if (app != null) {
                    findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToAppDetailFragment(app, false))
                }
                else {
                    showAlert(getString(R.string.dapp_not_found_text), getString(R.string.ok), {

                    }, getString(R.string.dapp_not_found_title))
                }
            }
        }

        return true
    }


    @SuppressLint("InflateParams", "SetTextI18n")
    override fun updateUtxos(utxoInfoList: List<UtxoInfoItem>, isEnablePrivacyMode: Boolean) {

    }

    override fun updatePaymentProof(paymentProof: PaymentProof) {
        proofCodeValueLabel.text = paymentProof.rawProof
    }

    @SuppressLint("SetTextI18n")
    override fun updateAddresses(txDescription: TxDescription) {

    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        val status = txDescription.getStatusString(requireContext()).trim()

        if (txDescription.isDapps == true) {
            senderLayout.visibility = View.GONE
            receiverLayout.visibility = View.GONE
            addressTypeLayout.visibility = View.GONE
        }
        else {
            dAppNameLayout.visibility = View.GONE
            dAppShaderLayout.visibility = View.GONE
        }

        if (txDescription.minConfirmationsProgress != null && txDescription.minConfirmationsProgress != "unknown") {
            confirmationLayout.visibility = View.VISIBLE
        }
        else {
            confirmationLayout.visibility = View.GONE
        }

        if (status == TxStatus.Failed.name.toLowerCase() || status == TxStatus.Cancelled.name.toLowerCase() || txDescription.failureReason == TxFailureReason.TRANSACTION_EXPIRED
            || txDescription.kernelId.startsWith("0000000")) {
            kernelButton.visibility = View.GONE
        }

        if (txDescription.kernelId.startsWith("0000000") || txDescription.kernelId.isEmpty()) {
            kernelLayout.visibility = View.GONE
        }

        if (txDescription.message.isEmpty()) {
            commentLayout.visibility = View.GONE
        }

        dateLabel.text = CalendarUtils.fromTimestamp(txDescription.createTime)
        startAddress.text = txDescription.senderAddress
        if((txDescription.isPublicOffline || txDescription.isMaxPrivacy || txDescription.isShielded) && (!txDescription.sender.value)) {
            startAddress.text = getString(R.string.shielded_pool)
            senderLayout.visibility = View.GONE
        }
        endAddress.text = txDescription.receiverAddress

        val start = AppManager.instance.getAddress(txDescription.senderAddress)

        if (start != null && start.label.isNotEmpty()) {
            startContactLayout.visibility = View.VISIBLE
            startContactValue.text = start.label
        } else {
            startContactLayout.visibility = View.GONE
        }

        val end = AppManager.instance.getAddress(txDescription.receiverAddress)
        if (end != null && end.label.isNotEmpty()) {
            endContactLayout.visibility = View.VISIBLE
            endContactValue.text = end.label
        } else {
            endContactLayout.visibility = View.GONE
        }

        addressTypeLabel.text = txDescription.getAddressType(requireContext())
        confirmationLabel.text = txDescription.getConfirmation(requireContext())

        val amount = txDescription.amount.convertToAssetString(txDescription.asset?.unitName ?: "")
        amountLabel.text = txDescription.prefix() + amount
        amountLabel.setTextColor(txDescription.amountColor())

        val secondAmount = txDescription.amount.exchangeValueAssetWithRate(txDescription.rate, txDescription.assetId)
        val rate = ExchangeManager.instance.currentCurrency().shortName()
        if (secondAmount.isEmpty()) {
            amountSecondLabel.text = getString(R.string.exchange_rate_not_available, rate)
        }
        else {
            amountSecondLabel.text = getString(R.string.exchange_rate_calculated, txDescription.prefix() + secondAmount)
        }
        amountAssetIcon.setImageResource(txDescription.asset?.image ?: R.drawable.ic_asset_0)

        assetIdLabel.text = (txDescription.asset?.assetId ?: 0).toString()

        if (txDescription.asset?.isBeam() == true) {
            assetIdLayout.visibility = View.GONE
        }
        else {
            assetIdLayout.visibility = View.VISIBLE
        }

        val fee = txDescription.fee.convertToAssetString("BEAM")
        val secondFee = txDescription.fee.exchangeValueAssetWithRate(txDescription.rate,0)
        feeLabel.text = fee
        if (secondFee.isEmpty()) {
            feeSecondLabel.visibility = View.GONE
        }
        else {
            feeSecondLabel.visibility = View.VISIBLE
            feeSecondLabel.text = secondFee
        }
        feeAssetIcon.setImageResource(R.drawable.ic_asset_0)

        dAppNameLabel.text = txDescription.appName
        dAppShaderLabel.text = txDescription.contractCids

        if (txDescription.isDapps == true) {
            commentTitleLabel.text = getString(R.string.description)
        }
        else {
            commentTitleLabel.text = getString(R.string.comment)
        }
        commentLabel.text = txDescription.message

        idLabel.text = txDescription.id
        kernelLabel.text = txDescription.kernelId

        dateTitleLabel.text = dateTitleLabel.text.toString() + ":"
        startAddressTitle.text = startAddressTitle.text.toString() + ":"
        endAddressTitle.text = endAddressTitle.text.toString() + ":"
        addressTypeTitleLabel.text = addressTypeTitleLabel.text.toString() + ":"
        confirmationTitleLabel.text = confirmationTitleLabel.text.toString() + ":"
        amountTitleLabel.text = amountTitleLabel.text.toString() + ":"
        feeTitleLabel.text = feeTitleLabel.text.toString() + ":"
        dAppNameTitleLabel.text = dAppNameTitleLabel.text.toString() + ":"
        dAppShaderTitleLabel.text = dAppShaderTitleLabel.text.toString() + ":"
        commentTitleLabel.text = commentTitleLabel.text.toString() + ":"
        transactionIdTitleLabel.text = transactionIdTitleLabel.text.toString() + ":"
        kernelTitleLabel.text = kernelTitleLabel.text.toString() + ":"
        failedTitle.text = failedTitle.text.toString() + ":"

        addressTypeProofTitleLabel.text = addressTypeTitleLabel.text.toString()
        kernelProofTitleLabel.text = kernelTitleLabel.text.toString()
        amountProofTitleLabel.text = amountTitleLabel.text.toString()


        receiverProofLayout.visibility = receiverLayout.visibility
        senderProofLayout.visibility = senderLayout.visibility

        endContactProofLayout.visibility = View.GONE
        startProofContactLayout.visibility = View.GONE

        endContactProofValue.text = endContactValue.text.toString()
        startProofContactValue.text = startContactValue.text.toString()

        addressTypeProofLabel.text = addressTypeLabel.text.toString()

        amountProofLabel.text = amountLabel.text.toString()
        amountProofLabel.setTextColor(amountLabel.textColors)

        amountProofSecondLabel.text = amountSecondLabel.text.toString()
        amountProofSecondLabel.visibility = amountSecondLabel.visibility
        amountProofAssetIcon.setImageResource(txDescription.asset?.image ?: R.drawable.ic_asset_0)

        kernelProofLayout.visibility = kernelLayout.visibility
        kernelProofButton.visibility = kernelButton.visibility
        kernelProofLabel.text = kernelLabel.text.toString()

        proofCodeTitleLabel.text = proofCodeTitleLabel.text.toString() + ":"

        if (!AppManager.instance.isToken(endAddress.text.toString())) {
            senderProofLayout.visibility = View.GONE
            receiverProofLayout.visibility = View.GONE
            addressTypeProofLayout.visibility = View.GONE

            val params = amountProofLayout.layoutParams as LinearLayout.LayoutParams
            params.topMargin = ScreenHelper.dpToPx(requireContext(), 10)
            amountProofLayout.layoutParams = params
        }

        addressTypeProofLayout.visibility = View.GONE

        startProofAddressTitle.text = getString(R.string.sender_wallet_signature) + ":"
        endAddressProofTitle.text = getString(R.string.receiver_wallet_signature) + ":"

        startProofAddress.text = txDescription.senderIdentity
        endProofAddress.text = txDescription.receiverIdentity

        if (txDescription.status == TxStatus.Failed) {
            failedLabel.text = txDescription.getTransactionFailedString(requireContext())
            failedLayout.visibility = View.VISIBLE
        }
        else {
            failedLayout.visibility = View.GONE
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

    override fun showOpenAssetIdLinkAlert() {
        showAlert(
            getString(R.string.common_external_link_dialog_message),
            getString(R.string.open),
            {
                openExternalLink(AppConfig.buildAssetIdLink( presenter?.state?.txDescription?.assetId ?: 0))
            },
            getString(R.string.common_external_link_dialog_title),
            getString(R.string.cancel)
        )
    }


    override fun addListeners() {
        assetIdLayout.setOnClickListener {
            presenter?.onOpenAssetIdPressed()
        }
        proofCodeCopyButton.setOnClickListener {
            copyToClipboard(proofCodeValueLabel.text.toString(), "")
            showSnackBar(getString(R.string.copied_to_clipboard))
        }

        kernelButton.setOnClickListener {
            presenter?.onOpenInBlockExplorerPressed()
        }

        kernelProofButton.setOnClickListener {
            presenter?.onOpenInBlockExplorerPressed()
        }

        copyIdLabel.setOnClickListener {
            copyToClipboard(idLabel.text.toString(), "")
            showSnackBar(getString(R.string.copied_to_clipboard))
        }

        startAddressCopyButton.setOnClickListener {
            copyToClipboard(startAddress.text.toString(), "")
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }

        endAddressCopyButton.setOnClickListener {
            copyToClipboard(endAddress.text.toString(), "")
            showSnackBar(getString(R.string.address_copied_to_clipboard))
        }

        startProofAddressCopyButton.setOnClickListener {
            copyToClipboard(startProofAddress.text.toString(), "")
            showSnackBar(getString(R.string.copied_to_clipboard))
        }

        endAddressProofCopyButton.setOnClickListener {
            copyToClipboard(endProofAddress.text.toString(), "")
            showSnackBar(getString(R.string.copied_to_clipboard))
        }

        registerForContextMenu(startAddress)
        registerForContextMenu(endAddress)
        registerForContextMenu(idLabel)
        registerForContextMenu(kernelLabel)
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val copy = SpannableStringBuilder()
        copy.append(getString(R.string.copy))
        copy.setSpan(ForegroundColorSpan(Color.WHITE),
                0, copy.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val share = SpannableStringBuilder()
        share.append(getString(R.string.share))
        share.setSpan(ForegroundColorSpan(Color.WHITE),
                0, share.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        menu.add(0, v.id, 0, copy)
        menu.add(0, v.id, 0, share)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        if (item.itemId == startAddress.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(startAddress.text.toString(), "")
                showSnackBar(getString(R.string.address_copied_to_clipboard))
            } else {
                shareText(startAddress.text.toString())
            }
        } else if (item.itemId == endAddress.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(endAddress.text.toString(), "")
                showSnackBar(getString(R.string.address_copied_to_clipboard))
            } else {
                shareText(endAddress.text.toString())
            }
        } else if (item.itemId == idLabel.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(idLabel.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(idLabel.text.toString())
            }
        }
        else if (item.itemId == kernelLabel.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(kernelLabel.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(kernelLabel.text.toString())
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun shareText(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
    }

    override fun showDeleteSnackBar(txDescription: TxDescription) {
        showSnackBar(getString(R.string.transaction_deleted),
                onDismiss = { TrashManager.remove(txDescription.id) },
                onUndo = { TrashManager.restore(txDescription.id) })
    }

    override fun showSendFragment(address: String, amount: Long) {
        AssetManager.instance.selectedAssetId = presenter?.state?.txDescription?.assetId ?: 0
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToSendFragment(address, amount))
    }

    override fun showReceiveFragment(amount: Long, walletAddress: WalletAddress?) {
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToReceiveFragment(amount, walletAddress))
    }

    override fun showPaymentProof(paymentProof: PaymentProof) {
        findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToPaymentProofDetailsFragment(paymentProof))
    }

    override fun showSaveContact(address: String?) {
        if (address != null) {
            findNavController().navigate(TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToAddContactFragment(address))
        }
    }

    override fun showCopiedAlert() {
        showSnackBar(getString(R.string.copied))
    }

    override fun clearListeners() {

    }

    override fun finishScreen() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return TransactionDetailsPresenter(this, TransactionDetailsRepository(), TransactionDetailsState())
    }

    override fun convertViewIntoBitmap(): Bitmap? {
        return share_transaction_details?.drawToBitmap()
    }

    override fun shareTransactionDetails(file: File?) {
        if (file != null) {
            val uri = FileProvider.getUriForFile(requireContext(), AppConfig.AUTHORITY, file)

            context?.apply {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }

                startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
            }

            mainContent.removeView(share_transaction_details)
        }
    }

    override fun handleExpandProof(shouldExpandProof: Boolean) {

    }

    override fun handleExpandUtxos(shouldExpandUtxos: Boolean) {

    }

    override fun handleExpandDetails(shouldExpandDetails: Boolean) {

    }

    override fun showCancelAlert() {
        showAlert(message = getString(R.string.cancell_transaction_warning_message),
                title = getString(R.string.cancel_transaction),
                btnConfirmText = getString(R.string.cancel),
                btnCancelText = getString(R.string.back),
                onConfirm = { presenter?.onCancelTransactionConfirm() })
    }

    override fun showDeleteAlert() {
        showAlert(message = getString(R.string.delete_transaction_text),
                title = getString(R.string.delete_transaction),
                btnConfirmText = getString(R.string.delete),
                btnCancelText = getString(R.string.cancel),
                onConfirm = { presenter?.onDeleteTransactionsPressed() })
    }

    override fun copyDetails() {
        val transaction = oldTransaction!!

        var txDetails = "${getString(R.string.date)}: ${dateLabel.text}\n" +
                "${getString(R.string.status)}: ${transaction.getStatusString(requireContext())}\n"

        if (senderLayout.visibility == View.VISIBLE) {
            txDetails +=  "${getString(R.string.sending_address)}: ${startAddress.text}\n"
        }

        if (receiverLayout.visibility == View.VISIBLE) {
            txDetails +=  "${getString(R.string.receiving_address)}: ${endAddress.text}\n"
        }

        if (dAppNameLayout.visibility == View.VISIBLE) {
            txDetails +=  "${getString(R.string.dapp_anme)}: ${dAppNameLabel.text}\n"
            txDetails +=  "${getString(R.string.app_shader_id)}: ${dAppShaderLabel.text}\n"
        }

        txDetails +=  "${getString(R.string.address_type)}: ${addressTypeLabel.text}\n"
        txDetails +=  "${getString(R.string.amount)}: ${amountLabel.text}\n"
        txDetails +=  "${getString(R.string.fee)}: ${feeLabel.text}\n"

        if (idLayout.visibility == View.VISIBLE) {
            txDetails += "${getString(R.string.transaction_id)}: ${transaction.id}\n"
        }

        if (kernelLayout.visibility == View.VISIBLE) {
            txDetails += "${getString(R.string.kernel_id)}: ${transaction.kernelId}\n"
        }

        if (failedLayout.visibility == View.VISIBLE) {
            txDetails += "${getString(R.string.failure_reason)}: ${transaction.getTransactionFailedString(requireContext())}\n"
        }

        copyToClipboard(txDetails, "")
        showSnackBar(getString(R.string.copied_to_clipboard))
    }

}
