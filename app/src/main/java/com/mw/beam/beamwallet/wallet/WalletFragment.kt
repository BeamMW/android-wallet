// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.wallet

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithCurrency
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import kotlinx.android.synthetic.main.fragment_wallet.*


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletFragment : BaseFragment<WalletPresenter>(), WalletContract.View {
    private lateinit var presenter: WalletPresenter
    private lateinit var adapter: TransactionsAdapter

    companion object {
        fun newInstance() = WalletFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WalletFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_wallet
    override fun getToolbarTitle(): String? = getString(R.string.wallet_title)

    override fun configWalletStatus(walletStatus: WalletStatus) {
        configAvailable(walletStatus.available)
        configInProgress(walletStatus.receiving, walletStatus.sending, walletStatus.maturing)
    }

    override fun configAvailable(availableAmount: Long) {
        available.text = availableAmount.convertToBeamWithCurrency()
    }

    override fun configInProgress(receivingAmount: Long, sendingAmount: Long, maturingAmount: Long) {
        if (receivingAmount == 0L && sendingAmount == 0L && maturingAmount == 0L) {
            inProgressLayout.visibility = View.GONE
            return
        } else {
            inProgressLayout.visibility = View.VISIBLE
        }

        when (receivingAmount) {
            0L -> {
                receivingGroup.visibility = View.GONE
            }
            else -> {
                receiving.text = receivingAmount.convertToBeamWithSign(false)
                receivingGroup.visibility = View.VISIBLE
            }
        }

        when (sendingAmount) {
            0L -> sendingGroup.visibility = View.GONE
            else -> {
                sending.text = sendingAmount.convertToBeamWithSign(true)
                sendingGroup.visibility = View.VISIBLE
            }
        }

        when (maturingAmount) {
            0L -> maturingGroup.visibility = View.GONE
            else -> {
                maturing.text = maturingAmount.convertToBeamWithCurrency()
                maturingGroup.visibility = View.VISIBLE
            }
        }
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        transactionsTitle.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

        if (transactions.isNotEmpty()) {
            adapter.setData(transactions)
        }
    }

    override fun init() {
        initTransactionsList()
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnReceive.setOnClickListener { presenter.onReceivePressed() }
        btnSend.setOnClickListener { presenter.onSendPressed() }

        btnExpandAvailable.setOnClickListener {
            presenter.onExpandAvailablePressed()
        }

        btnExpandInProgress.setOnClickListener {
            presenter.onExpandInProgressPressed()
        }

        btnTransactionsMenu.setOnClickListener { view ->
            presenter.onTransactionsMenuButtonPressed(view)
        }
    }

    private fun initTransactionsList() {
        val context = context ?: return

        adapter = TransactionsAdapter(context, mutableListOf(), object : TransactionsAdapter.OnItemClickListener {
            override fun onItemClick(item: TxDescription) {
                presenter.onTransactionPressed(item)
            }
        })

        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = adapter
    }

    override fun handleExpandAvailable(shouldExpandAvailable: Boolean) {
        animateDropDownIcon(btnExpandAvailable, !shouldExpandAvailable)
        availableGroup.visibility = if (shouldExpandAvailable) View.GONE else View.VISIBLE
    }

    override fun handleExpandInProgress(shouldExpandInProgress: Boolean) {
        animateDropDownIcon(btnExpandInProgress, !shouldExpandInProgress)
        receivingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
        sendingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
        maturingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
    }

    @SuppressLint("RestrictedApi")
    override fun showTransactionsMenu(menu: View) {
        val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)
        val transactionsMenu = PopupMenu(wrapper, menu)
        transactionsMenu.inflate(R.menu.wallet_transactions_menu)

        transactionsMenu.setOnMenuItemClickListener {
            presenter.onTransactionsMenuPressed(it)
        }

        val menuHelper = MenuPopupHelper(wrapper, transactionsMenu.menu as MenuBuilder, menu)
        menuHelper.setForceShowIcon(true)
        menuHelper.gravity = Gravity.START
        menuHelper.show()
    }

    override fun handleTransactionsMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                presenter.onSearchPressed()
                true
            }
            R.id.menu_filter -> {
                presenter.onFilterPressed()
                true
            }
            R.id.menu_export -> {
                presenter.onExportPressed()
                true
            }
            R.id.menu_delete -> {
                presenter.onDeletePressed()
                true
            }
            else -> true
        }
    }

    override fun showTransactionDetails(txDescription: TxDescription) = (activity as TransactionDetailsHandler).onShowTransactionDetails(txDescription)
    override fun showReceiveScreen() = (activity as TransactionDetailsHandler).onReceive()
    override fun showSendScreen() = (activity as TransactionDetailsHandler).onSend()

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnSend.setOnClickListener(null)
        btnExpandAvailable.setOnClickListener(null)
        btnExpandInProgress.setOnClickListener(null)
        btnTransactionsMenu.setOnClickListener(null)
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WalletPresenter(this, WalletRepository(), WalletState())
        return presenter
    }

    interface TransactionDetailsHandler {
        fun onShowTransactionDetails(item: TxDescription)
        fun onReceive()
        fun onSend()
    }
}
