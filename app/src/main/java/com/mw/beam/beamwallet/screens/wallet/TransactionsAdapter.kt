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
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import com.mw.beam.beamwallet.screens.transactions.TransactionsFragment
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*
import java.util.regex.Pattern


class TransactionsAdapter(private val context: Context, private val longListener: OnLongClickListener? = null, var data: List<TxDescription>, private val cellMode: TransactionsAdapter.Mode, private val clickListener: (TxDescription) -> Unit) :
        RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    enum class Mode {
        SHORT, FULL, SEARCH
    }

    private val colorSpan by lazy { ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)) }
    private val boldFontSpan by lazy { StyleSpan(Typeface.BOLD) }
    private val regularTypeface by lazy { ResourcesCompat.getFont(context, R.font.roboto_regular) }
    private val commonDarkTextColor by lazy { ContextCompat.getColor(context, R.color.common_text_dark_color) }
    private val itemOffset by lazy { context.resources.getDimensionPixelSize(R.dimen.search_text_offset) }
    private val receiveText = context.getString(R.string.receive)
    private val sendText = context.getString(R.string.send)
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

    override fun getItemId(position: Int): Long {
        val data = data[position]
        return data.createTime
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = data[position]

        holder.apply {
            val messageStatus = when (transaction.sender) {
                TxSender.RECEIVED -> receiveText
                TxSender.SENT -> sendText
            }

            message.text = messageStatus

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

            icon.setImageDrawable(transaction.statusImage())

            if (transaction.status == TxStatus.Failed) {
                icon.imageTintList = ColorStateList.valueOf(transaction.statusColor())
            }
            else {
                icon.imageTintList = null
            }

            sum.text = transaction.amount.convertToBeamWithSign(transaction.sender.value) + " BEAM"
            sum.setTextColor(transaction.amountColor())

            status.setTextColor(transaction.statusColor())
            status.text = transaction.getStatusString(context)

            if(sumSecondBalance!=null)
            {
                val amount = transaction.amount.convertToCurrencyString()
                if (amount == null) {
                    sumSecondBalance.text = amount
                }
                else {
                    if (transaction.sender.value) {
                        sumSecondBalance.text = "-$amount"
                    }
                    else{
                        sumSecondBalance.text = "+$amount"
                    }
                }
            }


            val amountVisibility = if (privacyMode) View.GONE else View.VISIBLE
            sum.visibility = amountVisibility
            sumSecondBalance.visibility = amountVisibility

            if (cellMode == Mode.SEARCH) {
                searchResultContainer.removeAllViews()
                searchResultContainer.visibility = if (searchString.isNullOrBlank()) View.GONE else View.VISIBLE

                searchString?.let { search ->

                    val txAddresses = AppManager.instance.getAllAddresses()?.filter { it.id == transaction.myId || it.id == transaction.peerId }

                    val findAddresses = txAddresses.filter { it.label.toLowerCase().contains(search.toLowerCase()) }

                    if (transaction.id.startsWith(search.toLowerCase())) {
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.transaction_id)}:", transaction.id, search)
                    }

                    if (transaction.peerId.startsWith(search.toLowerCase())) {
                        val title = context.getString(if (transaction.sender.value && !transaction.selfTx) R.string.contact else R.string.my_address)
                        addSearchTextItem(searchResultContainer, "$title:", transaction.peerId, search)
                    }

                    if (transaction.myId.startsWith(search.toLowerCase())) {
                        val title = context.getString(if (transaction.sender.value || transaction.selfTx) R.string.my_address else R.string.contact)
                        addSearchTextItem(searchResultContainer, "$title:", transaction.myId, search)
                    }

                    if (transaction.kernelId.startsWith(search.toLowerCase())) {
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.kernel_id)}:", transaction.kernelId, search)
                    }

                    if (findAddresses.isNotEmpty()) {
                        findAddresses.forEach {
                            addSearchIconItem(searchResultContainer, it, search)
                        }
                    }
                }
            }


            if (cellMode != Mode.SHORT) {
                date.text = CalendarUtils.fromTimestamp(transaction.createTime)

                when {
                    !searchString.isNullOrBlank() && transaction.message.toLowerCase().contains(searchString?.toLowerCase()
                            ?: "") && transaction.message.isNotBlank() -> {
                        commentIcon.visibility = View.VISIBLE
                        commentTextView.visibility = View.VISIBLE

                        setSpannableText(commentTextView, transaction.message, searchString ?: "")
                    }
                    transaction.message.isNotBlank() -> {
                        commentIcon.visibility = View.VISIBLE
                        commentTextView.text = transaction.message
                        commentTextView.setTextColor(ContextCompat.getColor(context, R.color.common_text_dark_color))
                        commentTextView.visibility = View.VISIBLE
                    }
                    else -> {
                        commentIcon.visibility = View.GONE
                        commentTextView.visibility = View.GONE
                    }
                }
            }

            if (cellMode == Mode.FULL) {
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

                    text = getSpannableFromText(address.label, search)

                    setPadding(itemOffset, 0, 0, 0)
                }

                addView(image)
                addView(textView)

                setPadding(0, itemOffset, 0, 0)
            })
        }
    }

    private fun addSearchTextItem(searchResultContainer: LinearLayout, title: String, content: String, search: String) {
        val textView = TextView(context, null, R.style.common_text_big_dark).apply {

            setTypeface(regularTypeface, Typeface.NORMAL)
            setTextColor(commonDarkTextColor)

            text = SpannableStringBuilder().apply {
                append(title.replace(" ", "\u00A0"), boldFontSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                append("\u00A0")
                append(getSpannableFromText(content.replace(" ", "\u00A0"), search))
            }

            setPadding(0, itemOffset, 0, 0)
        }
        searchResultContainer.addView(textView)
    }

    private fun setSpannableText(textView: TextView, allText: String, containedText: String) {
        textView.text = allText
        val spannable = getSpannableFromText(allText, containedText)

        textView.text = spannable
    }

    private fun getSpannableFromText(allText: String, containedText: String): SpannableStringBuilder {
        return SpannableStringBuilder().apply {
            append(allText)
            val matcher = Pattern.compile(containedText.toLowerCase()).matcher(allText.toLowerCase())
            if (matcher.find()) {
                setSpan(colorSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    fun setSearchText(text: String?) {
        if (searchString != text) {
            searchString = text
            notifyDataSetChanged()
        }
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