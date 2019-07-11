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
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import kotlinx.android.synthetic.main.fragment_wallet.*
import java.io.File


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletFragment : BaseFragment<WalletPresenter>(), WalletContract.View {
    private lateinit var adapter: TransactionsAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navItemsAdapter: NavItemsAdapter

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
                return
            }

            App.isAuthenticated = false
            activity?.finish()
        }
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_wallet
    override fun getToolbarTitle(): String? = getString(R.string.wallet)

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
            adapter.data = transactions
            adapter.notifyDataSetChanged()
        }
    }

    override fun init() {
        App.isAuthenticated = true

        initTransactionsList()
        setHasOptionsMenu(true)

        val toolbar = toolbarLayout.toolbar
        (activity as? BaseActivity<*>)?.setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        configNavView()
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnReceive.setOnClickListener { presenter?.onReceivePressed() }
        btnNext.setOnClickListener { presenter?.onSendPressed() }

        btnExpandAvailable.setOnClickListener {
            presenter?.onExpandAvailablePressed()
        }

        clickableAvailableArea.setOnClickListener {
            presenter?.onExpandAvailablePressed()
        }


        btnExpandInProgress.setOnClickListener {
            presenter?.onExpandInProgressPressed()
        }

        clickableInProgressArea.setOnClickListener {
            presenter?.onExpandInProgressPressed()
        }

        btnTransactionsMenu.setOnClickListener { view ->
            presenter?.onTransactionsMenuButtonPressed(view)
        }

        whereBuyBeamLink.setOnClickListener {
            presenter?.onWhereBuyBeamPressed()
        }
    }

    override fun addTitleListeners(isEnablePrivacyMode: Boolean) {
        if (!isEnablePrivacyMode) {
            availableTitle.setOnClickListener {
                presenter?.onExpandAvailablePressed()
            }

            inProgressTitle.setOnClickListener {
                presenter?.onExpandInProgressPressed()
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
                presenter?.onTransactionPressed(item)
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
    }

    private fun beginTransition() {
        TransitionManager.beginDelayedTransition(contentLayout, AutoTransition().apply {
            excludeChildren(transactionsList, true)
            excludeChildren(receivingCurrency, true)
            excludeChildren(sendingCurrency, true)
        })
    }

    @SuppressLint("RestrictedApi")
    override fun showTransactionsMenu(menu: View, emptyTransactionList: Boolean) {
        val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)
        val transactionsMenu = PopupMenu(wrapper, menu)
        transactionsMenu.inflate(R.menu.wallet_transactions_menu)

        transactionsMenu.setOnMenuItemClickListener {
            presenter?.onTransactionsMenuPressed(it) ?: false
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
                presenter?.onSearchPressed()
                true
            }
            R.id.menu_filter -> {
                presenter?.onFilterPressed()
                true
            }
            R.id.menu_export -> {
                presenter?.onExportPressed()
                true
            }
            R.id.menu_delete -> {
                presenter?.onDeletePressed()
                true
            }
            R.id.menu_proof -> {
                presenter?.onProofVerificationPressed()
                true
            }
            else -> true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onCreateOptionsMenu(menu, inflater)
    }

    override fun createOptionsMenu(menu: Menu?, inflater: MenuInflater?, isEnablePrivacyMode: Boolean) {
        inflater?.inflate(R.menu.privacy_menu, menu)
        val menuItem = menu?.findItem(R.id.privacy_mode)
        menuItem?.setOnMenuItemClickListener {
            presenter?.onChangePrivacyModePressed()
            false
        }

        menuItem?.setIcon(if (isEnablePrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.cancel), { presenter?.onCancelDialog() })
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

    override fun showTransactionDetails(txId: String) {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToTransactionDetailsFragment(txId))
    }
    override fun showReceiveScreen() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToReceiveFragment())
    }
    override fun showSendScreen() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToSendFragment())
    }

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnNext.setOnClickListener(null)
        btnExpandAvailable.setOnClickListener(null)
        btnExpandInProgress.setOnClickListener(null)
        btnTransactionsMenu.setOnClickListener(null)
        clickableAvailableArea.setOnClickListener(null)
        clickableInProgressArea.setOnClickListener(null)
        clearTitleListeners()
        whereBuyBeamLink.setOnClickListener(null)
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
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToProofVerificationFragment())
    }

    private fun configNavView() {
        val menuItems = arrayOf(
//                NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.nav_wallet), isSelected = true),
                NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.address_book)),
                NavItem(NavItem.ID.UTXO, R.drawable.menu_utxo, getString(R.string.utxo)),
                NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.settings)))

        navItemsAdapter = NavItemsAdapter(context!!, menuItems, object : NavItemsAdapter.OnItemClickListener {
            override fun onItemClick(navItem: NavItem) {
                drawerLayout.closeDrawer(GravityCompat.START)

                val direction = when (navItem.id) {
//                    NavItem.ID.WALLET -> showFragment(WalletFragment.newInstance(), WalletFragment.getFragmentTag(), WalletFragment.getFragmentTag(), true)
                    NavItem.ID.ADDRESS_BOOK -> WalletFragmentDirections.actionWalletFragmentToAddressesFragment()
                    NavItem.ID.UTXO -> WalletFragmentDirections.actionWalletFragmentToUtxoFragment()
//                    NavItem.ID.DASHBOARD -> LogUtils.log("dashboard")
//                    NavItem.ID.NOTIFICATIONS -> LogUtils.log("notifications")
//                    NavItem.ID.HELP -> LogUtils.log("help")
                    NavItem.ID.SETTINGS -> WalletFragmentDirections.actionWalletFragmentToSettingsFragment()
                    else -> null
                }

                if (direction != null) {
                    findNavController().navigate(direction)
                }
            }
        })
        navMenu.layoutManager = LinearLayoutManager(context)
        navMenu.adapter = navItemsAdapter

        navItemsAdapter.selectItem(NavItem.ID.WALLET)
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

    override fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onStart() {
        super.onStart()
        App.showNotification = false
    }

    override fun onStop() {
        App.showNotification = true
        super.onStop()
    }

    override fun clearAllNotification() {
        (context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancelAll()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WalletPresenter(this, WalletRepository(), WalletState())
    }
}
