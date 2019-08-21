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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.core.views.addDoubleDots
import kotlinx.android.synthetic.main.fragment_utxo_details.*
import kotlinx.android.synthetic.main.item_utxo.*

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsFragment : BaseFragment<UtxoDetailsPresenter>(), UtxoDetailsContract.View {

    override fun onControllerGetContentLayoutId() = R.layout.fragment_utxo_details
    override fun getToolbarTitle(): String? = getString(R.string.utxo_details)
    override fun getUtxo(): Utxo = UtxoDetailsFragmentArgs.fromBundle(arguments!!).utxo

    override fun init(utxo: Utxo) {
        configUtxoInfo(utxo)
        configUtxoDetails(utxo)

        kernelIdTitle.addDoubleDots()
        utxoTypeTitle.addDoubleDots()
        completionTimeTitle.addDoubleDots()
        contactTitle.addDoubleDots()
    }

    @SuppressLint("SetTextI18n")
    private fun configUtxoInfo(utxo: Utxo) {
        status.setTextColor(when (utxo.status) {
            UtxoStatus.Maturing, UtxoStatus.Incoming -> ContextCompat.getColor(context!!, R.color.received_color)
            UtxoStatus.Outgoing, UtxoStatus.Change, UtxoStatus.Spent -> ContextCompat.getColor(context!!, R.color.sent_color)
            UtxoStatus.Available, UtxoStatus.Unavailable -> ContextCompat.getColor(context!!, R.color.common_text_color)
        })

        status.text = when (utxo.status) {
            UtxoStatus.Incoming, UtxoStatus.Change, UtxoStatus.Outgoing -> getString(R.string.in_progress)
            UtxoStatus.Maturing -> getString(R.string.maturing)
            UtxoStatus.Spent -> getString(R.string.spent)
            UtxoStatus.Available -> getString(R.string.available)
            UtxoStatus.Unavailable -> getString(R.string.unavailable)
        }.toLowerCase() + " "

//        detailedStatus.visibility = View.VISIBLE
//        detailedStatus.text = "(" + when (utxo.status) {
//            UtxoStatus.Incoming -> getString(R.string.incoming)
//            UtxoStatus.Change -> getString(R.string.change)
//            UtxoStatus.Outgoing -> getString(R.string.outgoing)
//            UtxoStatus.Unavailable -> getString(R.string.utxo_status_result_rollback)
//            UtxoStatus.Maturing, UtxoStatus.Spent, UtxoStatus.Available -> {
//                detailedStatus.visibility = View.GONE
//                "" //TODO add correct description for maturing
//            }
//        }.toLowerCase() + ") "

        amount.text = utxo.amount.convertToBeamString()
        utxoLayout.findViewById<TextView>(R.id.addressId).text = utxo.stringId
    }

    private fun configUtxoDetails(utxo: Utxo) {
        utxoType.text = when (utxo.keyType) {
            UtxoKeyType.Commission -> getString(R.string.commission)
            UtxoKeyType.Coinbase -> getString(R.string.coinbase)
            UtxoKeyType.Regular -> getString(R.string.regular)
            UtxoKeyType.Change -> getString(R.string.change)
            UtxoKeyType.Kernel -> getString(R.string.kernel)
            UtxoKeyType.Kernel2 -> getString(R.string.kernel2)
            UtxoKeyType.Identity -> getString(R.string.identity)
            UtxoKeyType.ChildKey -> getString(R.string.childKey)
            UtxoKeyType.Bbs -> getString(R.string.bbs)
            UtxoKeyType.Decoy -> getString(R.string.decoy)
        }
    }

    override fun configUtxoHistory(utxo: Utxo, relatedTransactions: List<TxDescription>?) {
        val offset: Int = resources.getDimensionPixelSize(R.dimen.utxo_history_offset)

        utxoHistoryGroup.visibility = if (relatedTransactions.isNullOrEmpty()) View.GONE else View.VISIBLE

        transactionHistoryList.removeAllViews()
        relatedTransactions?.forEach {
            transactionHistoryList.addView(configTransaction(
                    isReceived = it.id == utxo.createTxId,
                    time = CalendarUtils.fromTimestamp(it.modifyTime),
                    id = it.id,
                    comment = it.message,
                    offset = offset))
        }
    }

    override fun configUtxoKernel(kernelIdString: String?) {
        if (kernelIdString.isNullOrEmpty()) {
            kernelId.visibility = View.GONE
            kernelIdTitle.visibility = View.GONE
        } else {
            kernelId.visibility = View.VISIBLE
            kernelIdTitle.visibility = View.VISIBLE
            kernelId.text = kernelIdString
        }
    }

    @SuppressLint("InflateParams")
    private fun configTransaction(isReceived: Boolean, time: String, id: String, comment: String, offset: Int): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.item_history, null)
        view.findViewById<TextView>(R.id.time).text = time
        view.findViewById<TextView>(R.id.addressId).text = id
        view.findViewById<ImageView>(R.id.icon).setImageResource(if (isReceived) R.drawable.ic_history_received else R.drawable.ic_history_sent)
        view.findViewById<TextView>(R.id.comment).apply {
            if (comment.isNotEmpty()) {
                text = comment
                visibility = View.VISIBLE
            }
        }

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = offset
        params.bottomMargin = offset

        view.layoutParams = params

        return view
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return UtxoDetailsPresenter(this, UtxoDetailsRepository(), UtxoDetailsState())
    }
}
