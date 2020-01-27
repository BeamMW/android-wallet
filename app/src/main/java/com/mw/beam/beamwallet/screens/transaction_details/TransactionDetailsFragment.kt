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
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import kotlinx.android.synthetic.main.item_transaction_utxo.view.*
import java.io.File
import com.mw.beam.beamwallet.core.AppManager
import android.transition.AutoTransition
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.graphics.drawable.GradientDrawable
import kotlinx.android.synthetic.main.fragment_transaction_details.detailsArrowView
import kotlinx.android.synthetic.main.fragment_transaction_details.detailsExpandLayout
import kotlinx.android.synthetic.main.fragment_transaction_details.kernelLayout
import kotlinx.android.synthetic.main.fragment_transaction_details.receiverLayout
import kotlinx.android.synthetic.main.fragment_transaction_details.senderLayout
import kotlinx.android.synthetic.main.fragment_transaction_details.toolbarLayout
import android.view.MenuInflater
import android.view.LayoutInflater
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableStringBuilder
import android.graphics.Color
import android.content.res.ColorStateList
import com.mw.beam.beamwallet.core.App


/**
 *  10/18/18.
 */
class TransactionDetailsFragment : BaseFragment<TransactionDetailsPresenter>(), TransactionDetailsContract.View {
    private var moreMenu: Menu? = null
    private var share_transaction_details: ShareTransactionDetailsView? = null

    private var oldTransaction: TxDescription? = null

    override fun onControllerGetContentLayoutId() = R.layout.fragment_transaction_details
    override fun getToolbarTitle(): String? = getString(R.string.transaction_details)
    override fun getTransactionId(): String = TransactionDetailsFragmentArgs.fromBundle(arguments!!).txId
    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)
}

    var downX: Float = 0f
    var downY: Float = 0f


    override fun init(txDescription: TxDescription, isEnablePrivacyMode: Boolean) {
        if (oldTransaction?.status != txDescription.status || dateLabel.text.isNullOrEmpty()) {
            oldTransaction = txDescription

            configGeneralTransactionInfo(txDescription)

            setHasOptionsMenu(true)

            activity?.invalidateOptionsMenu()

            moreMenu?.close()

            toolbarLayout.hasStatus = true

            amountLabel.visibility = if (isEnablePrivacyMode) View.GONE else View.VISIBLE

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
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.repeat -> presenter?.onRepeatTransaction()
            R.id.cancel -> presenter?.onCancelTransaction()
            R.id.delete -> presenter?.onDeleteTransaction()
            R.id.share -> {
                share_transaction_details = ShareTransactionDetailsView(context!!)

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


    @SuppressLint("InflateParams")
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
                    utxoView.utxoIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, colorRes))
                }
                else{
                    utxoView.utxoIcon.setImageDrawable(context?.getDrawable(drawableId))
                }

                utxoView.utxoAmount.text = utxo.amount.convertToBeamString()

                utxosList.addView(utxoView)
            }
        }
    }

    override fun updatePaymentProof(paymentProof: PaymentProof) {
        if (proofLayout.visibility == View.VISIBLE)
            return

        proofLabel.text = paymentProof.rawProof
        proofLayout.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    override fun updateAddresses(txDescription: TxDescription) {
        val start = AppManager.instance.getAddress(startAddress.text.toString())

        if (start != null && !start.label.isNullOrEmpty()) {
            startContactLayout.visibility = View.VISIBLE
            startContactValue.text = start.label
        } else {
            startContactLayout.visibility = View.GONE
        }

        val end = AppManager.instance.getAddress(endAddress.text.toString())
        if (end != null && !end.label.isNullOrEmpty()) {
            endContactLayout.visibility = View.VISIBLE
            endContactValue.text = end.label
        } else {
            endContactLayout.visibility = View.GONE
        }

        val startTags = TagHelper.getTagsForAddress(startAddress.text.toString())
        val endTags = TagHelper.getTagsForAddress(endAddress.text.toString())

        startAddressCategory.visibility = if (startTags.isEmpty()) View.GONE else View.VISIBLE
        startAddressCategory.text = startTags.createSpannableString(context!!)

        endAddressCategory.visibility = if (endTags.isEmpty()) View.GONE else View.VISIBLE
        endAddressCategory.text = endTags.createSpannableString(context!!)
    }

    @SuppressLint("SetTextI18n")
    private fun configGeneralTransactionInfo(txDescription: TxDescription) {
        when (txDescription.sender) {
            TxSender.RECEIVED -> currencyIcon.setImageResource(R.drawable.currency_beam_receive)
            TxSender.SENT -> currencyIcon.setImageResource(R.drawable.currency_beam_send)
        }

        dateLabel.text = CalendarUtils.fromTimestamp(txDescription.createTime)

        amountLabel.text = txDescription.amount.convertToBeamWithSign(txDescription.sender.value)
        amountLabel.setTextColor(txDescription.amountColor)

        statusLabel.setTextColor(txDescription.statusColor)
        val status = txDescription.getStatusString(context!!).trim()

        if (status == TxStatus.Failed.name.toLowerCase() || status == TxStatus.Cancelled.name.toLowerCase() || txDescription.failureReason == TxFailureReason.TRANSACTION_EXPIRED) {
            btnOpenInBlockExplorer.visibility = View.INVISIBLE
        }

        val upperString = status.substring(0, 1).toUpperCase() + status.substring(1)
        statusLabel.text = upperString

        transactionStatusIcon.setImageDrawable(txDescription.statusImage())

        val drawable = shape.background as GradientDrawable
        drawable.setStroke(ScreenHelper.dpToPx(context, 1), txDescription.statusColor)

        if (txDescription.sender.value) {
            if (txDescription.selfTx) {
                startAddress.text = txDescription.myId
                endAddress.text = txDescription.peerId

                startAddressTitle.text = "${getString(R.string.my_sending_address)}".toUpperCase()
                endAddressTitle.text = "${getString(R.string.my_receiving_address)}".toUpperCase()
            } else {
                startAddressTitle.text = "${getString(R.string.contact)}".toUpperCase()
                endAddressTitle.text = "${getString(R.string.my_address)}".toUpperCase()

                startAddress.text = txDescription.peerId
                endAddress.text = txDescription.myId
            }
        } else {
            startAddressTitle.text = "${getString(R.string.contact)}".toUpperCase()
            endAddressTitle.text = "${getString(R.string.my_address)}".toUpperCase()
            startAddress.text = txDescription.peerId
            endAddress.text = txDescription.myId
        }

        feeLabel.text = txDescription.fee.toString() + " GROTH"
        idLabel.text = txDescription.id
        kernelLabel.text = txDescription.kernelId

//        val externalLinkVisibility = if (isValidKernelId(txDescription.kernelId)) View.VISIBLE else View.GONE
//        btnOpenInBlockExplorer.visibility = externalLinkVisibility

        if (txDescription.message.isNotEmpty()) {
            commentLabel.text = txDescription.message
            commentLayout.visibility = View.VISIBLE
        } else {
            commentLayout.visibility = View.GONE
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
            val uri = FileProvider.getUriForFile(context!!, AppConfig.AUTHORITY, file)

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
        feeLayout.visibility = contentVisibility
        idLayout.visibility = contentVisibility
        kernelLayout.visibility = contentVisibility

        if (contentVisibility == View.VISIBLE && !commentLabel.text.toString().isNullOrEmpty()) {
            commentLayout.visibility = contentVisibility
        } else {
            commentLayout.visibility = contentVisibility
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

    override fun onResume() {
        super.onResume()
        dateLabel.text = CalendarUtils.fromTimestamp(oldTransaction?.createTime!!)
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
        val clipboardManager =  context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        val txDetails = "${getString(R.string.amount)}: ${oldTransaction!!.amount.div(100000000) }\n" +
                "${getString(R.string.status)}: ${statusLabel.text}\n" +
                "${getString(R.string.date)}: ${dateLabel.text}\n" +
                "${getString(R.string.contact)}: ${startContactValue.text}\n" +
                "${startAddress.text}\n" +
                "${getString(R.string.my_address)}: ${endAddress.text}\n" +
                "${getString(R.string.transaction_fee)}: ${feeLabel.text}\n" +
                "${getString(R.string.transaction_id)}: ${idLabel.text}\n" +
                "${getString(R.string.kernel_id)}: ${kernelLabel.text}\n"

        val clip = ClipData.newPlainText("label", txDetails)
        clipboardManager?.setPrimaryClip(clip)
        showSnackBar(getString(R.string.copied_to_clipboard))
    }

}
