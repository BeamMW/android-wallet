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
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*
import java.util.regex.Pattern

/**
 * Created by vain onnellinen on 10/2/18.
 */
class TransactionsAdapter(private val context: Context, var data: List<TxDescription>, private val compactMode: Boolean, private val clickListener: (TxDescription) -> Unit) :
        androidx.recyclerview.widget.RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private val sendIconId = R.drawable.ic_icon_sent
    private val receivedIconId = R.drawable.ic_icon_received
    private val colorSpan by lazy { ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)) }
    private val boldFontSpan by lazy { StyleSpan(Typeface.BOLD) }
    private val regularTypeface by lazy { ResourcesCompat.getFont(context, R.font.roboto_regular) }
    private val commonDarkTextColor by lazy { ContextCompat.getColor(context, R.color.common_text_dark_color) }
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.colorClear)
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val receiveText = context.getString(R.string.receive)
    private val sendText = context.getString(R.string.send)
    private var privacyMode: Boolean = false
    private var searchString: String? = null
    var addresses: List<WalletAddress>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.invoke(data[adapterPosition])
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

            itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor) //logically reversed because count starts from zero
            icon.setImageResource(if (transaction.sender.value) sendIconId else receivedIconId)
            date.text = CalendarUtils.fromTimestamp(transaction.createTime)
            currency.setImageDrawable(transaction.currencyImage)

            sum.text = transaction.amount.convertToBeamWithSign(transaction.sender.value)
            sum.setTextColor(transaction.amountColor)

            status.setTextColor(transaction.statusColor)
            status.text = transaction.getStatusString(context)

            val amountVisibility = if (privacyMode) View.GONE else View.VISIBLE
            sum.visibility = amountVisibility
            currency.visibility = amountVisibility

            searchResultContainer.removeAllViews()
            searchResultContainer.visibility = if (searchString.isNullOrBlank()) View.GONE else View.VISIBLE

            val txAddresses = addresses?.filter { it.walletID == transaction.myId || it.walletID == transaction.peerId }
                    ?: listOf()

            searchString?.let { search ->
                val findAddresses = txAddresses.filter { it.label.toLowerCase().contains(search.toLowerCase()) }

                when {
                    transaction.id.startsWith(search.toLowerCase()) -> {
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.transaction_id)}:", transaction.id, search)
                    }
                    transaction.peerId.startsWith(search.toLowerCase()) -> {
                        val title = context.getString(if (transaction.sender.value && !transaction.selfTx) R.string.contact else R.string.my_address)
                        addSearchTextItem(searchResultContainer, "$title:", transaction.peerId, search)
                    }
                    transaction.myId.startsWith(search.toLowerCase()) -> {
                        val title = context.getString(if (transaction.sender.value || transaction.selfTx) R.string.my_address else R.string.contact)
                        addSearchTextItem(searchResultContainer, "$title:", transaction.myId, search)
                    }
                    transaction.kernelId.startsWith(search.toLowerCase()) -> {
                        addSearchTextItem(searchResultContainer, "${context.getString(R.string.kernel_id)}:", transaction.kernelId, search)
                    }
                    findAddresses.isNotEmpty() -> {
                        findAddresses.forEach {
                            addSearchIconItem(searchResultContainer, it, search)
                        }
                    }
                    else -> {
                    }
                }
            }


            if (compactMode) {
                commentIcon.visibility = View.GONE
                commentTextView.visibility = View.GONE
                date.visibility = View.GONE
            } else {
                date.visibility = View.VISIBLE

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

                    setPadding(context.resources.getDimensionPixelSize(R.dimen.search_text_offset), 0, 0, 0)
                }

                addView(image)
                addView(textView)
            })
        }
    }

    private fun addSearchTextItem(searchResultContainer: LinearLayout, title: String, content: String, search: String) {
        val textView = TextView(context, null, R.style.common_text_big_dark).apply {

            setTypeface(regularTypeface, Typeface.NORMAL)
            setTextColor(commonDarkTextColor)

            text = SpannableStringBuilder().apply {
                append(title, boldFontSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(" ")
                append(getSpannableFromText(content, search))
            }
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

    class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer
}
