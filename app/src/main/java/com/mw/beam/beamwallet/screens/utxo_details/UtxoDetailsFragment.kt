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

package com.mw.beam.beamwallet.screens.utxo_details

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import kotlinx.android.synthetic.main.fragment_utxo_details.*
import kotlinx.android.synthetic.main.fragment_utxo_details.toolbarLayout
import android.transition.TransitionManager
import android.transition.AutoTransition
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.helpers.selector
import com.mw.beam.beamwallet.core.utils.CalendarUtils

/**
 *  12/20/18.
 */
class UtxoDetailsFragment : BaseFragment<UtxoDetailsPresenter>(), UtxoDetailsContract.View {

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)
    }
    else{
        ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_utxo_details
    override fun getToolbarTitle(): String? = getString(R.string.utxo_details)
    override fun getUtxo(): Utxo = UtxoDetailsFragmentArgs.fromBundle(arguments!!).utxo


    override fun init(utxo: Utxo) {
        configUtxoInfo(utxo)

        detailsArrowView.rotation = 180f
    }

    @SuppressLint("SetTextI18n")
    private fun configUtxoInfo(utxo: Utxo) {

        val toolbarLayout = toolbarLayout
        toolbarLayout.hasStatus = true

        idLabel.text = utxo.stringId

        amountLabel.text = utxo.amount.convertToBeamString()

        val status = when (utxo.status) {
            UtxoStatus.Incoming -> getString(R.string.incoming)
            UtxoStatus.Change -> getString(R.string.change_utxo_type)
            UtxoStatus.Outgoing -> getString(R.string.outgoing)
            UtxoStatus.Maturing -> getString(R.string.maturing)
            UtxoStatus.Spent -> getString(R.string.spent)
            UtxoStatus.Available -> getString(R.string.available)
            UtxoStatus.Unavailable -> getString(R.string.unavailable)
        }

        val receivedColor = ContextCompat.getColor(context!!, R.color.received_color)
        val sentColor = ContextCompat.getColor(context!!, R.color.sent_color)
        val commonStatusColor = ContextCompat.getColor(context!!, R.color.common_text_color)

        if (utxo.status == UtxoStatus.Maturing) {
            val available = getString(R.string.maturing)
            val till = " (" + getString(R.string.till_block_height) + " " + utxo.maturity + ")"
            val string = available + till

            val spannable = SpannableStringBuilder.valueOf(string)
            spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.common_text_color, context?.theme)),
                    0,available.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            statusLabel.text = spannable
        }
        else if(utxo.confirmHeight > 0) {
            val till = " (" + getString(R.string.since_block_height).toLowerCase() + " " + utxo.confirmHeight + ")"
            val string = status + till

            val spannable = SpannableStringBuilder.valueOf(string)

            when (utxo.status) {
                UtxoStatus.Incoming ->  spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.received_color, context?.theme)),
                        0,status.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                UtxoStatus.Outgoing, UtxoStatus.Spent ->  spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.sent_color, context?.theme)),
                        0,status.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                UtxoStatus.Change, UtxoStatus.Maturing, UtxoStatus.Available, UtxoStatus.Unavailable ->  spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.common_text_color, context?.theme)),
                        0,status.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }

            statusLabel.text = spannable
        }
        else{
            statusLabel.text = status
            statusLabel.setTextColor(when (utxo.status) {
                UtxoStatus.Incoming -> receivedColor
                UtxoStatus.Outgoing, UtxoStatus.Spent -> sentColor
                UtxoStatus.Change, UtxoStatus.Maturing, UtxoStatus.Available, UtxoStatus.Unavailable -> commonStatusColor
            })
        }

            typeLabel.text = when (utxo.keyType) {
            UtxoKeyType.Commission -> getString(R.string.commission)
            UtxoKeyType.Coinbase -> getString(R.string.coinbase)
            UtxoKeyType.Regular -> getString(R.string.regular)
            UtxoKeyType.Change -> getString(R.string.change_utxo_type)
            UtxoKeyType.Kernel -> getString(R.string.kernel)
            UtxoKeyType.Kernel2 -> getString(R.string.kernel2)
            UtxoKeyType.Identity -> getString(R.string.identity)
            UtxoKeyType.ChildKey -> getString(R.string.childKey)
            UtxoKeyType.Bbs -> getString(R.string.bbs)
            UtxoKeyType.Decoy -> getString(R.string.decoy)
            UtxoKeyType.Treasury -> getString(R.string.treasure)
        }
    }


    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        detailsExpandLayout.setOnClickListener {
            presenter?.onExpandDetailedPressed()
        }

        transactionsExpandLayout.setOnClickListener {
            presenter?.onExpandTransactionsPressed()
        }
    }

    override fun configUtxoHistory(utxo: Utxo, relatedTransactions: List<TxDescription>?) {
        val offset: Int = resources.getDimensionPixelSize(R.dimen.utxo_history_offset)
        var index = 0

        transactionsLayout.visibility = if (relatedTransactions.isNullOrEmpty()) View.GONE else View.VISIBLE

        transactionHistoryList.removeAllViews()
        relatedTransactions?.forEach {
            transactionHistoryList.addView(configTransaction(
                    isReceived = it.id == utxo.createTxId,
                    time = CalendarUtils.fromTimestamp(it.createTime),
                    id = it.id,
                    comment = it.message,
                    offset = offset,
                    index = index))
            index++
        }
    }

    override fun handleExpandDetails(shouldExpandDetails: Boolean) {
        animateDropDownIcon(detailsArrowView, !shouldExpandDetails)
        beginTransition()

        val contentVisibility = if (shouldExpandDetails) View.VISIBLE else View.GONE
        idLayout.visibility = contentVisibility
        typeLayout.visibility = contentVisibility
    }

    override fun handleExpandTransactions(shouldExpandTransactions: Boolean) {

    }

    @SuppressLint("InflateParams")
    private fun configTransaction(isReceived: Boolean, time: String, id: String, comment: String, offset: Int, index:Int): View? {
        val notMultiplyColor = ContextCompat.getColor(context!!, R.color.colorClear)

        val multiplyColor = if (App.isDarkMode) {
            ContextCompat.getColor(context!!, R.color.wallet_adapter_multiply_color_dark)
        }
        else{
            ContextCompat.getColor(context!!, R.color.wallet_adapter_multiply_color)
        }

        val view = LayoutInflater.from(context).inflate(R.layout.item_history, null)
        view.findViewById<TextView>(R.id.date).text = time
        view.findViewById<ImageView>(R.id.icon).setImageResource(if (isReceived) R.drawable.ic_history_received else R.drawable.ic_history_sent)
        view.findViewById<TextView>(R.id.status).text = if (isReceived) getString(R.string.receive) else getString(R.string.send)

        if (comment.isNullOrEmpty())
        {
            view.findViewById<ConstraintLayout>(R.id.commentLayout).visibility = View.GONE
        }
        else{
            view.findViewById<ConstraintLayout>(R.id.commentLayout).visibility = View.VISIBLE

            view.findViewById<TextView>(R.id.commentLabel).text = "“" + comment + "“"
        }

        view.setBackgroundColor(if (index % 2 == 0) notMultiplyColor else multiplyColor)

        view.tag = index
        view.setOnClickListener {
            val index = it.tag as Int
            val transactionID = presenter?.state?.sortedTransactions()?.get(index)?.id
            if (transactionID != null) {
                findNavController().navigate(UtxoDetailsFragmentDirections.actionUtxoDetailsFragmentToTransactionDetailsFragment(transactionID))
            }
        }

        return view
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

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return UtxoDetailsPresenter(this, UtxoDetailsRepository(), UtxoDetailsState())
    }
}
