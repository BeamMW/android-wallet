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

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.content.FileProvider
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.TextView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import com.mw.beam.beamwallet.screens.proof_verification.ProofVerificationActivity
import kotlinx.android.synthetic.main.fragment_wallet.*
import java.io.File


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

    override fun configWalletStatus(walletStatus: WalletStatus, isEnablePrivacyMode: Boolean) {
        configAvailable(walletStatus.available, walletStatus.maturing, isEnablePrivacyMode)
        configInProgress(walletStatus.receiving, walletStatus.sending, isEnablePrivacyMode)
    }

    override fun configAvailable(availableAmount: Long, maturingAmount: Long, isEnablePrivacyMode: Boolean) {
        available.text = availableAmount.convertToBeamString()
        setTextColorWithPrivacyMode(availableTitle, isEnablePrivacyMode)

        maturingGroup.visibility = if (maturingAmount == 0L || isEnablePrivacyMode) View.GONE else View.VISIBLE

        when (maturingAmount) {
            0L -> maturingGroup.visibility = View.GONE
            else -> {
                maturing.text = maturingAmount.convertToBeamString()
                maturingGroup.visibility = View.VISIBLE
            }
        }
    }

    private fun setTextColorWithPrivacyMode(view: TextView, isEnablePrivacyMode: Boolean) {
        val colorId = if (isEnablePrivacyMode) R.color.common_text_dark_color else R.color.common_text_color
        view.setTextColor(resources.getColor(colorId, context?.theme))
    }

    override fun configInProgress(receivingAmount: Long, sendingAmount: Long, isEnablePrivacyMode: Boolean) {
        //nothing in progress
        if (receivingAmount == 0L && sendingAmount == 0L) {
            inProgressLayout.visibility = View.GONE
            return
        } else {
            inProgressLayout.visibility = View.VISIBLE
        }

        setTextColorWithPrivacyMode(inProgressTitle, isEnablePrivacyMode)

        if (isEnablePrivacyMode) {
            return
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
    }

    override fun configTransactions(transactions: List<TxDescription>, isEnablePrivacyMode: Boolean) {
        transactionsList.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        emptyTransactionsListMessage.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE

        if (transactions.isNotEmpty()) {
            adapter.setPrivacyMode(isEnablePrivacyMode)
            adapter.setData(transactions)
        }
    }

    override fun init() {
        initTransactionsList()
        setHasOptionsMenu(true)
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnReceive.setOnClickListener { presenter.onReceivePressed() }
        btnSend.setOnClickListener { presenter.onSendPressed() }

        btnExpandAvailable.setOnClickListener {
            presenter.onExpandAvailablePressed()
        }

        clickableAvailableArea.setOnClickListener {
            presenter.onExpandAvailablePressed()
        }


        btnExpandInProgress.setOnClickListener {
            presenter.onExpandInProgressPressed()
        }

        clickableInProgressArea.setOnClickListener {
            presenter.onExpandInProgressPressed()
        }

        btnTransactionsMenu.setOnClickListener { view ->
            presenter.onTransactionsMenuButtonPressed(view)
        }
    }

    override fun addTitleListeners(isEnablePrivacyMode: Boolean) {
        if (!isEnablePrivacyMode) {
            availableTitle.setOnClickListener {
                presenter.onExpandAvailablePressed()
            }

            inProgressTitle.setOnClickListener {
                presenter.onExpandInProgressPressed()
            }
        }
    }

    private fun clearTitleListeners() {
        inProgressTitle.setOnClickListener(null)
        availableTitle.setOnClickListener(null)
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
        beginTransition()
        availableGroup.visibility = if (shouldExpandAvailable) View.GONE else View.VISIBLE
        maturingGroup.visibility = if (shouldExpandAvailable) View.GONE else View.VISIBLE
    }

    override fun handleExpandInProgress(shouldExpandInProgress: Boolean) {
        animateDropDownIcon(btnExpandInProgress, !shouldExpandInProgress)
        beginTransition()
        receivingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
        sendingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
        maturingGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
    }

    private fun beginTransition() {
        TransitionManager.beginDelayedTransition(contentLayout, AutoTransition().apply { excludeChildren(transactionsList, true) })
    }

    @SuppressLint("RestrictedApi")
    override fun showTransactionsMenu(menu: View, emptyTransactionList: Boolean) {
        val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)
        val transactionsMenu = PopupMenu(wrapper, menu)
        transactionsMenu.inflate(R.menu.wallet_transactions_menu)

        transactionsMenu.setOnMenuItemClickListener {
            presenter.onTransactionsMenuPressed(it)
        }

        transactionsMenu.menu.findItem(R.id.menu_export)?.isVisible = !emptyTransactionList

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
            R.id.menu_proof -> {
                presenter.onProofVerificationPressed()
                true
            }
            else -> true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        presenter.onCreateOptionsMenu(menu, inflater)
    }

    override fun createOptionsMenu(menu: Menu?, inflater: MenuInflater?, isEnablePrivacyMode: Boolean) {
        inflater?.inflate(R.menu.privacy_menu, menu)
        val menuItem = menu?.findItem(R.id.privacy_mode)
        menuItem?.setOnMenuItemClickListener {
            presenter.onChangePrivacyModePressed()
            false
        }

        menuItem?.setIcon(if (isEnablePrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.common_activate), presenter::onPrivacyModeActivated, getString(R.string.common_security_mode_title), getString(R.string.common_cancel), presenter::onCancelDialog)
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        activity?.invalidateOptionsMenu()
        adapter.setPrivacyMode(isEnable)

        privacyGroupAvailable.visibility = if (isEnable) View.GONE else View.VISIBLE
        privacyGroupInProgress.visibility = if (isEnable) View.GONE else View.VISIBLE

        setTextColorWithPrivacyMode(availableTitle, isEnable)
        setTextColorWithPrivacyMode(inProgressTitle, isEnable)

        clearTitleListeners()
        addTitleListeners(isEnable)
    }

    override fun showTransactionDetails(txDescription: TxDescription) = (activity as WalletHandler).onShowTransactionDetails(txDescription)
    override fun showReceiveScreen() = (activity as WalletHandler).onReceive()
    override fun showSendScreen() = (activity as WalletHandler).onSend()

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnSend.setOnClickListener(null)
        btnExpandAvailable.setOnClickListener(null)
        btnExpandInProgress.setOnClickListener(null)
        btnTransactionsMenu.setOnClickListener(null)
        clickableAvailableArea.setOnClickListener(null)
        clickableInProgressArea.setOnClickListener(null)
        clearTitleListeners()
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun showShareFileChooser(file: File) {
        val context = context ?: return

        val uri = FileProvider.getUriForFile(context, AppConfig.AUTHORITY, file)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
    }

    override fun showProofVerification() {
        startActivity(Intent(context, ProofVerificationActivity::class.java))
    }

    override fun onStart() {
        super.onStart()
        App.showNotification = false
    }

    override fun onStop() {
        App.showNotification = true
        super.onStop()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = WalletPresenter(this, WalletRepository(), WalletState())
        return presenter
    }

    interface WalletHandler {
        fun onShowTransactionDetails(item: TxDescription)
        fun onReceive()
        fun onSend()
    }
}
