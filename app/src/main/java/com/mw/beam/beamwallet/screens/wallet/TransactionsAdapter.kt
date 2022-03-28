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

package com.mw.beam.beamwallet.screens.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.transactions.TransactionsFragment

import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*

import java.util.regex.Pattern
import android.graphics.Paint.FontMetricsInt

import android.text.style.LineHeightSpan




class TransactionsAdapter(private val context: Context, private val longListener: OnLongClickListener? = null, var data: List<TxDescription>, private val cellMode: TransactionsAdapter.Mode, private val clickListener: (TxDescription) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Mode {
        SHORT, FULL, SEARCH
    }


    private val colorSpan by lazy { ForegroundColorSpan(ContextCompat.getColor(context, R.color.received_color)) }
    private val whiteColor by lazy { ForegroundColorSpan(ContextCompat.getColor(context, R.color.white_100)) }

    private val boldFontSpan by lazy { StyleSpan(Typeface.BOLD) }
    private val regularTypeface by lazy { ResourcesCompat.getFont(context, R.font.roboto_regular) }
    private val commonDarkTextColor by lazy {
        if (App.isDarkMode) {
            ContextCompat.getColor(context, R.color.common_text_dark_color_dark)
        }
        else {
            ContextCompat.getColor(context, R.color.common_text_dark_color)
        }
    }
    private val itemOffset by lazy { context.resources.getDimensionPixelSize(R.dimen.search_text_offset) }
    private var privacyMode: Boolean = false
    private var searchString: String? = null

    var selectedTransactions = mutableListOf<String>()
    var mode = TransactionsFragment.Mode.NONE

    var reverseColors = false

    private fun getRowId() : Int {
        return when (cellMode) {
            Mode.SHORT -> R.layout.item_transaction_short
            Mode.FULL -> R.layout.item_transaction_full
            else -> R.layout.item_transaction_search
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(getRowId(), parent, false)).apply {
        this.containerView.setOnClickListener {

            if (adapterPosition>-1)
            {
                clickListener.invoke(data[adapterPosition])

                if (mode == TransactionsFragment.Mode.EDIT) {
                    if (selectedTransactions.contains(data[adapterPosition].id)) {
                        selectedTransactions.remove(data[adapterPosition].id)
                    } else {
                        selectedTransactions.add(data[adapterPosition].id)
                    }

                    checkBox.isChecked = selectedTransactions.contains(data[adapterPosition].id)
                }
            }

        }

        if (longListener != null) {
            this.containerView.setOnLongClickListener {
                longListener?.onLongClick(data[adapterPosition])
                return@setOnLongClickListener true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = (viewHolder as ViewHolder)
//        holder.setIsRecyclable(false)

        val transaction = data[position]

        holder.apply {

            val asset = transaction.asset

            assetIcon.setImageResource(asset?.image ?: R.drawable.ic_asset_0)

            when (transaction.sender) {
                TxSender.RECEIVED -> amountLabel.text = "+" + transaction.amount.convertToAssetString(asset?.unitName ?: "")
                TxSender.SENT -> amountLabel.text = "-" + transaction.amount.convertToAssetString(asset?.unitName ?: "")
            }

            if (privacyMode) {
                secondBalanceLabel.visibility = View.GONE
                secondBalanceLabel.text = ""
            }
            else {
                secondBalanceLabel.visibility = View.VISIBLE

                val secondValue = transaction.amount.exchangeValueAssetWithRate(transaction.rate, transaction.assetId)
                if (secondValue.isNotEmpty()) {
                    when (transaction.sender) {
                        TxSender.RECEIVED -> secondBalanceLabel.text = "+$secondValue"
                        TxSender.SENT -> secondBalanceLabel.text = "-$secondValue"
                    }
                }
            }

            if (App.isDarkMode) {
                if (reverseColors) {
                    itemView.selector(if (position % 2 == 0) R.color.colorClear else R.color.wallet_adapter_multiply_color_dark)
                }
                else{
                    itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_multiply_color_dark else R.color.colorClear)
                }
            }
            else{
              if (reverseColors) {
                  itemView.selector(if (position % 2 == 0) R.color.colorClear else R.color.wallet_adapter_multiply_color)
              }
              else{
                  itemView.selector(if (position % 2 == 0) R.color.wallet_adapter_multiply_color else R.color.colorClear)
              }
          }

            statusLabel.setCompoundDrawablesWithIntrinsicBounds(transaction.statusImage(), null, null, null)
            statusLabel.setTextColor(transaction.statusColor())
            statusLabel.text = transaction.getStatusString(context)

            if (cellMode == Mode.SEARCH) {
                searchResultContainer.removeAllViews()
                searchResultContainer.visibility = if (searchString.isNullOrBlank()) View.GONE else View.VISIBLE

                searchString?.let { search ->

                    var added = false

                    val txAddresses = AppManager.instance.getAllAddresses()?.filter { it.id == transaction.myId
                            || it.id == transaction.peerId
                            || it.id == transaction.token}

                    val findAddresses = arrayListOf<WalletAddress>()
                    findAddresses.addAll(txAddresses.filter { it.label.toLowerCase().contains(search.toLowerCase()) })

                    if (transaction.id.startsWith(search.toLowerCase())) {
                        added = true
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.transaction_id).uppercase()}:", "", "")
                        addSearchTextItem(searchResultContainer, "", transaction.id, search)
                    }

                    if (transaction.receiverAddress.lowercase().startsWith(search.toLowerCase())) {
                        var findAddressesReceive = txAddresses.filter { transaction.receiverAddress == it.id ||
                            transaction.token == it.address }
                        findAddressesReceive = findAddressesReceive.filter {
                            it.label.isNotEmpty()
                        }
                        if (findAddressesReceive.isEmpty()) {
                            if (added) {
                                addSearchTextItem(searchResultContainer, "", "", "")
                            }
                            added = true

                            val title = context.getString(R.string.receiving_address).uppercase()
                            addSearchTextItem(searchResultContainer, "$title:", "", "")
                            addSearchTextItem(searchResultContainer, "", transaction.receiverAddress, search)
                        }
                        else {
                            findAddresses.addAll(findAddressesReceive)
                        }
                    }

                    if (transaction.senderAddress.lowercase().startsWith(search.toLowerCase())) {
                        var findAddressesSend = txAddresses.filter {
                            ( transaction.senderAddress == it.id ||
                                    transaction.token == it.address) && !it.isContact
                        }
                        findAddressesSend = findAddressesSend.filter {
                            it.label.isNotEmpty()
                        }
                        if (findAddressesSend.isEmpty()) {
                            if (added) {
                                addSearchTextItem(searchResultContainer, "", "", "")
                            }
                            added = true

                            val title = context.getString(R.string.sending_address).uppercase()
                            addSearchTextItem(searchResultContainer, "$title:", "", "")
                            addSearchTextItem(searchResultContainer, "", transaction.senderAddress, search)
                        }
                        else {
                            findAddresses.addAll(findAddressesSend)
                        }
                    }


                    if (transaction.kernelId.lowercase().startsWith(search.toLowerCase())) {
                        if (added) {
                            addSearchTextItem(searchResultContainer, "", "", "")
                        }
                        added = true
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.kernel_id).uppercase()}:", "", "")
                        addSearchTextItem(searchResultContainer, "", transaction.kernelId, search)
                    }

                    if (findAddresses.isNotEmpty()) {
                        findAddresses.forEach {
                            if (added) {
                                addSearchTextItem(searchResultContainer, "", "", "")
                            }
                            added = true
                            if(transaction.senderAddress.startsWith(it.id) || transaction.senderAddress.startsWith(it.address)) {
                                val title = context.getString(R.string.sending_address).uppercase()
                                addSearchTextItem(searchResultContainer, "$title:", "", "")
                            }
                            else if(transaction.receiverAddress.startsWith(it.id) || transaction.receiverAddress.startsWith(it.address)) {
                                val title = context.getString(R.string.receiving_address).uppercase()
                                addSearchTextItem(searchResultContainer, "$title:", "", "")
                            }

                            addSearchIconItem(searchResultContainer, it, search)

                            if(transaction.senderAddress.startsWith(it.id) || transaction.senderAddress.startsWith(it.address)) {
                                addSearchTextItem(searchResultContainer, "", transaction.senderAddress, search)
                            }
                            else if(transaction.receiverAddress.startsWith(it.id) || transaction.receiverAddress.startsWith(it.address)) {
                                addSearchTextItem(searchResultContainer, "", transaction.receiverAddress, search)
                            }
                        }
                    }

                    if (transaction.message.toLowerCase().contains(searchString?.toLowerCase()
                            ?: "") && transaction.message.isNotBlank()) {
                        if (added) {
                            addSearchTextItem(searchResultContainer, "", "", "")
                        }
                        added = true
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.comment).uppercase()}:", "", "")
                        addSearchTextItem(searchResultContainer, "", transaction.message, search)
                    }
                }
            }


            if (cellMode != Mode.SHORT) {
                dateLabel.text = CalendarUtils.fromTimestamp(transaction.createTime)

                when {
                    transaction.message.isNotBlank() -> {
                        commentIcon.visibility = View.VISIBLE
                        commentLabel.text = transaction.message
                        commentLabel.setTextColor(ContextCompat.getColor(context, R.color.common_text_dark_color))
                        commentLabel.visibility = View.VISIBLE
                    }
                    else -> {
                        commentIcon.visibility = View.GONE
                        commentLabel.visibility = View.GONE
                    }
                }
            }

            if (cellMode == Mode.SEARCH) {
                checkBox.visibility = View.GONE
            }
            else if (cellMode == Mode.FULL) {
                checkBox.setOnClickListener {

                    if (selectedTransactions.contains(data[adapterPosition].id)) {
                        selectedTransactions.remove(data[adapterPosition].id)
                    } else {
                        selectedTransactions.add(data[adapterPosition].id)
                    }

                    clickListener.invoke(transaction)
                }

                if (mode == TransactionsFragment.Mode.NONE) {
                    checkBox.isChecked = false
                    checkBox.visibility = View.GONE
                } else {
                    checkBox.isChecked = selectedTransactions.contains(transaction.id)
                    checkBox.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun addSearchIconItem(searchResultContainer: LinearLayout, address: WalletAddress, search: String) {
        searchResultContainer.apply {
            addView(LinearLayout(context).apply {
                val image = ImageView(context).apply {
                    setImageResource(R.drawable.ic_contact)
                }

                val textView = TextView(context, null, R.style.common_text_big_dark).apply {
                    setTypeface(regularTypeface, Typeface.NORMAL)
                    setTextColor(commonDarkTextColor)
                    setVerticalGravity(Gravity.CENTER)
                    text = getSpannableFromText(address.label, search)

                    setPadding(context.resources.getDimensionPixelSize(R.dimen.search_text_offset), 0, 0, 0)
                }

                addView(image)
                addView(textView)

                setPadding(0, itemOffset, 0, 0)
            })
        }
    }

    private fun addSearchTextItem(searchResultContainer: LinearLayout, title: String, content: String, search: String) {
        if (title.isEmpty() && content.isEmpty()) {
            val view = View(context)
            view.layoutParams = ViewGroup.LayoutParams(100, ScreenHelper.dpToPx(context, 10))
            searchResultContainer.addView(view)
        }
        else {
            val textView = TextView(context, null, R.style.common_text_big_dark).apply {

                setTypeface(regularTypeface, Typeface.NORMAL)
                setTextColor(commonDarkTextColor)

                text = SpannableStringBuilder().apply {
                    append(title, boldFontSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(getSpannableFromText(content, search))
                }

                if (title.isNotEmpty() || content.isNotEmpty()) {
                    setPadding(0, itemOffset, 0, 0)
                }
            }
            if (title.isNotEmpty() && content.isEmpty()) {
                textView.letterSpacing = 0.13f
            }
            searchResultContainer.addView(textView)
        }
    }

    private fun getSpannableFromText(allText: String, containedText: String): SpannableStringBuilder {
        return SpannableStringBuilder().apply {
            append(allText)
            val matcher = Pattern.compile(containedText.toLowerCase()).matcher(allText.toLowerCase())
            setSpan(whiteColor, 0, allText.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            if (matcher.find()) {
                setSpan(colorSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    fun setSearchText(text: String?, transactions: List<TxDescription>) {
        if (searchString != text) {
            searchString = text
        }

        data = transactions
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    fun setPrivacyMode(isEnable: Boolean) {
        if (privacyMode != isEnable) {
            privacyMode = isEnable
            notifyDataSetChanged()
        }
    }

    fun item(index: Int): TxDescription {
        return data[index]
    }

    interface OnLongClickListener {
        fun onLongClick(item: TxDescription)
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    }
}