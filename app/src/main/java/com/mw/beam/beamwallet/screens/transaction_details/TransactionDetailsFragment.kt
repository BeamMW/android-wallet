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
import android.transition.TransitionManager
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.*
import androidx.navigation.fragment.findNavController
import android.transition.AutoTransition
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.view.MenuInflater
import android.view.LayoutInflater
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableStringBuilder
import android.graphics.Color
import android.content.res.ColorStateList

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
import kotlinx.android.synthetic.main.item_transaction_utxo.view.*

/**
 *  10/18/18.
 */
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

            detailsArrowView.rotation = 180f
            proofArrowView.rotation = 180f
            utxosArrowView.rotation = 180f
        }
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

            if (transaction.selfTx) {
                menu?.findItem(R.id.saveContact)?.isVisible = false
            } else {
                val contact = AppManager.instance.getAddress(transaction.peerId)
                menu?.findItem(R.id.saveContact)?.isVisible = contact == null
            }

            menu?.findItem(R.id.cancel)?.isVisible = TxStatus.InProgress == txStatus || TxStatus.Pending == txStatus
            menu?.findItem(R.id.delete)?.isVisible = TxStatus.Failed == txStatus || TxStatus.Completed == txStatus || TxStatus.Cancelled == txStatus
            menu?.findItem(R.id.repeat)?.isVisible = isSend && TxStatus.InProgress != txStatus && txStatus != TxStatus.Registered
                    && transaction.isMaxPrivacy == false && transaction.isPublicOffline == false && transaction.isMaxPrivacy == false
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
        }

        return true
    }


    @SuppressLint("InflateParams", "SetTextI18n")
    override fun updateUtxos(utxoInfoList: List<UtxoInfoItem>, isEnablePrivacyMode: Boolean) {
        utxosLayout.visibility = if (utxoInfoList.isEmpty() || isEnablePrivacyMode) View.GONE else View.VISIBLE


        if (utxosList.childCount != utxoInfoList.count()) {
            utxosList.removeAllViews()

            utxoInfoList.forEach { utxo ->
                val utxoView = LayoutInflater.from(context).inflate(R.layout.item_transaction_utxo, null)

                val drawableId = when (utxo.type) {
                    UtxoType.Send -> R.drawable.ic_history_sent
                    UtxoType.Receive -> R.drawable.ic_history_received
                    UtxoType.Exchange -> R.drawable.menu_utxo
                }

                if(utxo.type == UtxoType.Exchange) {
                    utxoView.utxoIcon.setImageDrawable(context?.getDrawable(drawableId))
                    val colorRes  = if (App.isDarkMode) {
                        R.color.common_text_dark_color_dark
                    }
                    else{
                        R.color.common_text_dark_color
                    }
                    utxoView.utxoIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes))
                }
                else{
                    utxoView.utxoIcon.setImageDrawable(context?.getDrawable(drawableId))
                }

                utxoView.utxoAmount.text = utxo.amount.convertToAssetStringWithId(utxo?.asset)

                utxosList.addView(utxoView)
            }
        }
    }

    override fun updatePaymentProof(paymentProof: PaymentProof) {
        if (proofLayout.visibility == View.VISIBLE)
            return
        else if(paymentProof.rawProof.isEmpty())
            return

        proofLabel.text = paymentProof.rawProof
        proofLayout.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    override fun updateAddresses(txDescription: TxDescription) {
        val start = AppManager.instance.getAddress(startAddress.text.toString())

        if (start != null && start.label.isNotEmpty()) {
            startContactLayout.visibility = View.VISIBLE
            startContactValue.text = start.label
        } else {
            startContactLayout.visibility = View.GONE
        }

        val end = AppManager.instance.getAddress(endAddress.text.toString())
        if (end != null && end.label.isNotEmpty()) {
            endContactLayout.visibility = View.VISIBLE
            endContactValue.text = end.label
        } else {
            endContactLayout.visibility = View.GONE
        }


        startAddressCategory.visibility = View.GONE
        endAddressCategory.visibility =  View.GONE
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun configGeneralTransactionInfo(txDescription: TxDescription) {

        dateLabel.text = CalendarUtils.fromTimestamp(txDescription.createTime)

        val amount = txDescription.amount.convertToAssetString(txDescription.asset?.unitName ?: "")
        amountLabel.text = amount
        amountLabel.setTextColor(txDescription.amountColor())

        val second = txDescription.amount.exchangeValueAsset(txDescription.assetId)

        if (second.isEmpty()) {
            secondAvailableSum.visibility = View.GONE
        }
        else {
            secondAvailableSum.text = second
        }

        statusLabel.setTextColor(txDescription.statusColor())
        val status = txDescription.getStatusString(requireContext()).trim()

        if (status == TxStatus.Failed.name.toLowerCase() || status == TxStatus.Cancelled.name.toLowerCase() || txDescription.failureReason == TxFailureReason.TRANSACTION_EXPIRED
                || kernelLabel.text.startsWith("0000000")) {
            btnOpenInBlockExplorer.visibility = View.GONE
        }

        val upperString = status.substring(0, 1).toUpperCase() + status.substring(1)
        statusLabel.text = upperString
        statusLabel.setCompoundDrawablesWithIntrinsicBounds(txDescription.statusImage(), null, null, null)

        assetIcon.setImageResource(txDescription.asset?.image ?: R.drawable.ic_asset_0)

        if (txDescription.sender.value) {
            if (txDescription.selfTx) {
                startAddress.text = txDescription.myId
                endAddress.text = txDescription.peerId

                startAddressTitle.text = getString(R.string.my_sending_address).toUpperCase()
                endAddressTitle.text = getString(R.string.my_receiving_address).toUpperCase()
            } else {
                startAddressTitle.text = getString(R.string.contact).toUpperCase()
                endAddressTitle.text = getString(R.string.my_address).toUpperCase()

                startAddress.text = txDescription.peerId
                endAddress.text = txDescription.myId

                if (txDescription.peerId.isEmpty()) {
                    startAddress.text = txDescription.token
                }
            }
        } else {
            startAddressTitle.text = getString(R.string.contact).toUpperCase()
            endAddressTitle.text = getString(R.string.my_address).toUpperCase()
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        feeLabel.text = txDescription.fee.toString() + " GROTH"
        idLabel.text = txDescription.id
        kernelLabel.text = txDescription.kernelId
        addressTypeLabel.text = txDescription.getAddressType(requireContext())

        if((txDescription.isPublicOffline || txDescription.isMaxPrivacy || txDescription.isShielded) && (!txDescription.sender.value)) {
            startAddress.text = getString(R.string.shielded_pool)
        }
//        else if((txDescription.isPublicOffline || txDescription.isMaxPrivacy || txDescription.isShielded) && (txDescription.sender.value)) {
//            endAddress.text = getString(R.string.shielded_pool)
//        }

        if(!txDescription.identity.isNullOrEmpty() && txDescription.identity != "0") {
            walletIdLayout.visibility = View.VISIBLE
            walletIdLabel.text = txDescription.identity
        }

        if (txDescription.message.isNotEmpty()) {
            commentLabel.text = txDescription.message
            commentLayout.visibility = View.VISIBLE
        } else {
            commentLayout.visibility = View.GONE
        }

        if (txDescription.fee <= 0L) {
            feeLayout.visibility = View.GONE
        }

        if (kernelLabel.text.startsWith("0000000") || kernelLabel.text.isEmpty()) {
            kernelLayout.visibility = View.GONE
            btnOpenInBlockExplorer.visibility = View.GONE
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
        btnOpenInBlockExplorer.setOnClickListener {
            presenter?.onOpenInBlockExplorerPressed()
        }

        detailsExpandLayout.setOnClickListener {
            presenter?.onExpandDetailedPressed()
        }

        utxosExpandLayout.setOnClickListener {
            presenter?.onExpandUtxosPressed()
        }

        proofExpandLayout.setOnClickListener {
            presenter?.onExpandProofPressed()
        }

        registerForContextMenu(startAddress)
        registerForContextMenu(endAddress)
        registerForContextMenu(idLabel)
        registerForContextMenu(proofLabel)
        registerForContextMenu(kernelLabel)
        registerForContextMenu(walletIdLabel)

//        var listener = View.OnTouchListener(function = {view, motionEvent ->
//
//            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
//                downX = view.getX() - motionEvent.getRawX();
//                downY = motionEvent.getRawY()
//            }
//            else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
//                var x =  motionEvent.getRawX() + downX
//
//                if(x<0) {
//                    x = 0f
//                }
//
//                view.animate()
//                        .x(x)
//                        .y(view.y)
//                        .setDuration(0)
//                        .start()
//
//
//            }
//            else if (motionEvent.action == MotionEvent.ACTION_UP) {
//                if (view.x > view.width/3) {
//                    findNavController().popBackStack()
//                }
//                else{
//                    view.animate()
//                            .x( 0f)
//                            .y(view.y)
//                            .setDuration(0)
//                            .start()
//                }
//            }
//
//                true
//
//        })
//        mainScroll.setOnTouchListener(listener)
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
        } else if (item.itemId == proofLabel.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(proofLabel.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(proofLabel.text.toString())
            }
        } else if (item.itemId == kernelLabel.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(kernelLabel.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(kernelLabel.text.toString())
            }
        }
        else if (item.itemId == walletIdLabel.id) {
            if (item.title.toString() == getString(R.string.copy)) {
                copyToClipboard(walletIdLabel.text.toString(), "")
                showSnackBar(getString(R.string.copied_to_clipboard))
            } else {
                shareText(walletIdLabel.text.toString())
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
        btnOpenInBlockExplorer.setOnClickListener(null)
        detailsExpandLayout.setOnClickListener(null)
        utxosExpandLayout.setOnClickListener(null)
        proofExpandLayout.setOnClickListener(null)
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
        animateDropDownIcon(proofArrowView, !shouldExpandProof)
        beginTransition()

        val contentVisibility = if (shouldExpandProof) View.VISIBLE else View.GONE
        proofValueLayout.visibility = contentVisibility
    }

    override fun handleExpandUtxos(shouldExpandUtxos: Boolean) {
        animateDropDownIcon(utxosArrowView, !shouldExpandUtxos)
        beginTransition()

        val contentVisibility = if (shouldExpandUtxos) View.VISIBLE else View.GONE
        utxosList.visibility = contentVisibility
    }

    override fun handleExpandDetails(shouldExpandDetails: Boolean) {
        animateDropDownIcon(detailsArrowView, !shouldExpandDetails)
        beginTransition()

        val contentVisibility = if (shouldExpandDetails) View.VISIBLE else View.GONE
        dateLayout.visibility = contentVisibility
        senderLayout.visibility = contentVisibility
        receiverLayout.visibility = contentVisibility
        idLayout.visibility = contentVisibility
        addressTypeLayout.visibility = contentVisibility
       // identityLayout.visibility = contentVisibility

        if (contentVisibility == View.VISIBLE && !commentLabel.text.toString().isNullOrEmpty()) {
            commentLayout.visibility = contentVisibility
        } else {
            commentLayout.visibility = View.GONE
        }

        if (presenter?.state?.txDescription?.fee ?: 0 <= 0L) {
            feeLayout.visibility = View.GONE
        }
        else {
            feeLayout.visibility = contentVisibility
        }

        if (presenter?.state?.txDescription?.identity.isNullOrEmpty()) {
            walletIdLayout.visibility = View.GONE
        }
        else {
            walletIdLayout.visibility = contentVisibility
        }

        if (kernelLabel.text.startsWith("0000000")) {
            kernelLayout.visibility = View.GONE
            btnOpenInBlockExplorer.visibility = View.GONE
        }
        else {
            kernelLayout.visibility = contentVisibility
            btnOpenInBlockExplorer.visibility = contentVisibility
        }
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    private fun beginTransition() {
        TransitionManager.beginDelayedTransition(mainConstraintLayout, AutoTransition().apply {
        })
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
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        var startAddressTitle = ""
        var endAddressTitle = ""

        var startAddress = ""
        var endAddress = ""

        val transaction = oldTransaction!!

        if (transaction.sender.value) {
            if (transaction.selfTx) {
                startAddress = transaction.myId
                endAddress = transaction.peerId

                startAddressTitle = "${getString(R.string.my_sending_address)}"
                endAddressTitle = "${getString(R.string.my_receiving_address)}"
            } else {
                startAddressTitle = "${getString(R.string.contact)}"
                endAddressTitle = "${getString(R.string.my_address)}"

                startAddress = transaction.peerId
                endAddress = transaction.myId

                if(startAddress.isEmpty()) {
                    startAddress = transaction.token
                }
            }
        }
        else {
            startAddressTitle = "${getString(R.string.contact)}"
            endAddressTitle = "${getString(R.string.my_address)}"
            startAddress = transaction.peerId
            endAddress = transaction.myId
        }

        if(startAddress.startsWith("100000")) {
            startAddress = "Shielded pool";
        }
        if(endAddress.startsWith("100000")) {
            endAddress = "Shielded pool";
        }

        var txDetails = "${getString(R.string.date)}: ${dateLabel.text}\n" +
                "${getString(R.string.status)}: ${statusLabel.text}\n" +
                "${getString(R.string.amount)}: ${amountLabel.text}\n" +
                startAddressTitle + ": " + startAddress + "\n" +
                endAddressTitle + ": " + endAddress + "\n"


        if(feeLabel.text.startsWith("0")) {

        }
        else {
            txDetails += "${getString(R.string.transaction_fee)}: ${feeLabel.text}\n"
        }

        txDetails += "${getString(R.string.transaction_id)}: ${idLabel.text}\n"

        if(!kernelLabel.text.startsWith("0000")) {
            txDetails += "${getString(R.string.kernel_id)}: ${kernelLabel.text}\n"
        }


        val clip = ClipData.newPlainText("label", txDetails)
        clipboardManager?.setPrimaryClip(clip)
        showSnackBar(getString(R.string.copied_to_clipboard))
    }

}
