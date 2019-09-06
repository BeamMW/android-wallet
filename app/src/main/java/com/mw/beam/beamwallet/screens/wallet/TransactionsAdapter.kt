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
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_transaction.*
import java.util.regex.Pattern
import androidx.asynclayoutinflater.view.AsyncLayoutInflater

/**
 *  10/2/18.
 */
class TransactionsAdapter(private val context: Context, var data: List<TxDescription>, private val compactMode: Boolean, private val clickListener: (TxDescription) -> Unit) :
        androidx.recyclerview.widget.RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {
    private val colorSpan by lazy { ForegroundColorSpan(context.resources.getColor(R.color.common_text_color, context.theme)) }
    private val notMultiplyColor = ContextCompat.getColor(context, R.color.colorClear)
    private val multiplyColor = ContextCompat.getColor(context, R.color.wallet_adapter_multiply_color)
    private val receiveText = context.getString(R.string.receive)
    private val sendText = context.getString(R.string.send)
    private var privacyMode: Boolean = false
    private var searchString: String? = null

    var reverseColors = false

    val asyncInflater = AsyncLayoutInflater(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)).apply {
            this.containerView.setOnClickListener {
                clickListener.invoke(data[adapterPosition])
            }
        }
        return view
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

            if (reverseColors) {
                itemView.setBackgroundColor(if (position % 2 == 0) notMultiplyColor else multiplyColor)
            }
            else{
                itemView.setBackgroundColor(if (position % 2 == 0) multiplyColor else notMultiplyColor)
            }

            icon.setImageDrawable(transaction.statusImage())

            date.text = CalendarUtils.fromTimestamp(transaction.modifyTime)
            currency.setImageDrawable(transaction.currencyImage)

            sum.text = transaction.amount.convertToBeamWithSign(transaction.sender.value)
            sum.setTextColor(transaction.amountColor)

            status.setTextColor(transaction.statusColor)
            status.text = transaction.getStatusString(context)

            val amountVisibility = if (privacyMode) View.GONE else View.VISIBLE
            sum.visibility = amountVisibility
            currency.visibility = amountVisibility

            var isContainsSearchString = false
            searchString?.let { search ->
                if (search.isNotBlank()) {

                    isContainsSearchString = when {
                        transaction.id.contains(search) -> {
                            setSpannableText(searchTextView, transaction.id, search)
                            true
                        }
                        transaction.peerId.contains(search) -> {
                            setSpannableText(searchTextView, transaction.peerId, search)
                            true
                        }
                        transaction.myId.contains(search) -> {
                            setSpannableText(searchTextView, transaction.myId, search)
                            true
                        }
                        transaction.message.contains(search) -> {
                            setSpannableText(searchTextView, transaction.message, search)
                            true
                        }
                        else -> false
                    }

                }
            }

            if (compactMode) {
                commentIcon.visibility = View.GONE
                searchTextView.visibility = View.GONE
                date.visibility = View.GONE
            } else {
                date.visibility = View.VISIBLE

                when {
                    isContainsSearchString -> {
                        commentIcon.visibility = View.VISIBLE
                        searchTextView.visibility = if (isContainsSearchString) View.VISIBLE else View.GONE
                    }
                    transaction.message.isNotBlank() -> {
                        commentIcon.visibility = View.VISIBLE
                        searchTextView.text = transaction.message
                        searchTextView.setTextColor(ContextCompat.getColor(context, R.color.common_text_dark_color))
                        searchTextView.visibility = View.VISIBLE
                    }
                    else -> {
                        commentIcon.visibility = View.GONE
                        searchTextView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setSpannableText(textView: TextView, allText: String, containedText: String) {
        textView.text = allText
        val spannable = SpannableStringBuilder().apply {
            append(allText)
        }

        val matcher = Pattern.compile(containedText).matcher(allText)
        if (matcher.find()) {
            spannable.setSpan(colorSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        textView.text = spannable
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
