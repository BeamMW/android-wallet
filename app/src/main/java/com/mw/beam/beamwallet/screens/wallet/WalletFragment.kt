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
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
import kotlinx.android.synthetic.main.fragment_wallet.*
import com.mw.beam.beamwallet.core.AppManager
import android.widget.PopupMenu
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.toolbar.*
import android.content.Intent
import com.mw.beam.beamwallet.screens.confirm.DoubleAuthorizationFragment
import com.mw.beam.beamwallet.core.OnboardManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.views.gone


/**
 *  10/1/18.
 */
class WalletFragment : BaseFragment<WalletPresenter>(), WalletContract.View {
    private lateinit var adapter: TransactionsAdapter
    private lateinit var balancePagerAdapter: BalancePagerAdapter

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if ((activity as? AppActivity)?.isMenuOpened() == true) {
                (activity as? AppActivity)?.closeMenu()
            }
            else{
                val setIntent = Intent(Intent.ACTION_MAIN)
                setIntent.addCategory(Intent.CATEGORY_HOME)
                setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(setIntent)
            }
        }
    }

    private val onPageSelectedListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val selectedColor = if (App.isDarkMode) {
                ContextCompat.getColor(context!!, R.color.common_text_dark_color_dark)
            } else{
                ContextCompat.getColor(context!!, R.color.common_text_dark_color)
            }

            val unselectedColor = ContextCompat.getColor(context!!, R.color.unselect_balance_tab_text_color)

            when (BalanceTab.values()[position]) {
                BalanceTab.Available -> {
                    availableTitle.setTextColor(selectedColor)
                    maturingTitle.setTextColor(unselectedColor)
                }
                BalanceTab.Maturing -> {
                    availableTitle.setTextColor(unselectedColor)
                    maturingTitle.setTextColor(selectedColor)
                }
            }
        }
    }

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)
}
else{
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)
}
    override fun onControllerGetContentLayoutId() = R.layout.fragment_wallet
    override fun getToolbarTitle(): String? = getString(R.string.wallet)

    override fun configWalletStatus(walletStatus: WalletStatus, expandBalanceCard: Boolean, expandInProgressCard: Boolean, isEnablePrivacyMode: Boolean) {
        configAvailable(walletStatus.available, walletStatus.maturing, expandBalanceCard, isEnablePrivacyMode)
        configInProgress(walletStatus.receiving, walletStatus.sending, expandInProgressCard, isEnablePrivacyMode)
    }

    override fun configAvailable(availableAmount: Long, maturingAmount: Long, expandCard: Boolean, isEnablePrivacyMode: Boolean) {
        balanceViewPager.adapter = balancePagerAdapter
        indicator.setViewPager(balanceViewPager)
        balancePagerAdapter.available = availableAmount
        balancePagerAdapter.maturing = maturingAmount
        balancePagerAdapter.notifyDataSetChanged()

        val contentVisibility = if (expandCard && !isEnablePrivacyMode) View.VISIBLE else View.GONE
        balanceViewPager.visibility = contentVisibility
        indicator.visibility = if (maturingAmount > 0 ) contentVisibility else View.GONE
        maturingTitle.visibility = if (maturingAmount > 0) View.VISIBLE else View.GONE
    }

    override fun configInProgress(receivingAmount: Long, sendingAmount: Long, expandCard: Boolean, isEnablePrivacyMode: Boolean) {
        //nothing in progress
        if (receivingAmount == 0L && sendingAmount == 0L) {
            inProgressLayout.visibility = View.GONE
            return
        } else {
            inProgressLayout.visibility = View.VISIBLE
        }

        if (isEnablePrivacyMode) {
            return
        }

        when (receivingAmount) {
            0L -> {
                receivingGroup.visibility = View.GONE
            }
            else -> {
                receiving.text = receivingAmount.convertToBeamWithSign(false)
                receiving.requestLayout()
                receiving.refreshDrawableState()
                receivingGroup.visibility = if (expandCard) View.VISIBLE else View.GONE
            }
        }

        when (sendingAmount) {
            0L -> sendingGroup.visibility = View.GONE
            else -> {
                sending.text = sendingAmount.convertToBeamWithSign(true)
                sendingGroup.visibility = if (expandCard) View.VISIBLE else View.GONE
            }
        }
    }

    override fun configTransactions(transactions: List<TxDescription>, isEnablePrivacyMode: Boolean) {
        transactionsList.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        emptyTransactionsListMessage.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        btnShowAll.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        transactionsTitle.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

        if (transactions.isNotEmpty()) {
            adapter.setPrivacyMode(isEnablePrivacyMode)
            adapter.data = transactions
            adapter.notifyDataSetChanged()

            btnShowAll.text = getString(R.string.show_all)
            btnShowAll.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null)
        }
    }

    override fun showAllTransactions() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToTransactionsFragment())
    }

    override fun init() {
        initTransactionsList()
        setHasOptionsMenu(true)

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }

        balancePagerAdapter = BalancePagerAdapter(context!!)
        balanceViewPager.adapter = balancePagerAdapter
        indicator.setViewPager(balanceViewPager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        itemsswipetorefresh.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

        itemsswipetorefresh.setOnRefreshListener {
            AppManager.instance.reload()
            android.os.Handler().postDelayed({
                itemsswipetorefresh.isRefreshing = false
            }, 1000)
        }
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnReceive.setOnClickListener { presenter?.onReceivePressed() }
        btnNext.setOnClickListener { presenter?.onSendPressed() }

        btnExpandAvailable.setOnClickListener {
            presenter?.onExpandAvailablePressed()
        }

        availableTitle.setOnClickListener {
            balanceViewPager.setCurrentItem(BalanceTab.Available.ordinal, true)
        }

        maturingTitle.setOnClickListener {
            balanceViewPager.setCurrentItem(BalanceTab.Maturing.ordinal, true)
        }

        balanceViewPager.addOnPageChangeListener(onPageSelectedListener)
        onPageSelectedListener.onPageSelected(balanceViewPager.currentItem)

        btnExpandInProgress.setOnClickListener {
            presenter?.onExpandInProgressPressed()
        }

        btnShowAll.setOnClickListener {

            val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)

            if (AppManager.instance.getTransactions().count() > 0) {
                presenter?.onShowAllPressed()
            }
            else PopupMenu(wrapper, btnShowAll).apply {
                setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToProofVerificationFragment())
                        return true
                    }
                })
                inflate(R.menu.proof_menu)
                show()
            }
        }

        btnFaucetClose.setOnClickListener {
            OnboardManager.instance.isCloseFaucet = true
            faucetLayout.gone(true)
        }

        btnSecureClose.setOnClickListener {
            OnboardManager.instance.isCloseSecure = true
            secureLayout.gone(true)
        }

        btnFaucetReceive.setOnClickListener {
            presenter?.onReceiveFaucet()
        }

        btnSecureReceive.setOnClickListener {
            presenter?.onSecure()
        }
    }

    override fun addTitleListeners(isEnablePrivacyMode: Boolean) {
        if (!isEnablePrivacyMode) {
            availableTitleContainer.setOnClickListener {
                presenter?.onExpandAvailablePressed()
            }

            inProgressTitleContainer.setOnClickListener {
                presenter?.onExpandInProgressPressed()
            }
        }
    }

    private fun clearTitleListeners() {
        inProgressTitleContainer.setOnClickListener(null)
        availableTitleContainer.setOnClickListener(null)
    }

    private fun initTransactionsList() {
        val context = context ?: return

        adapter = TransactionsAdapter(context, null, mutableListOf(), TransactionsAdapter.Mode.SHORT) {
            presenter?.onTransactionPressed(it)
        }

        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = adapter
    }

    override fun handleExpandAvailable(shouldExpandAvailable: Boolean) {
        animateDropDownIcon(btnExpandAvailable, !shouldExpandAvailable)
        beginTransition()

        val contentVisibility = if (shouldExpandAvailable) View.VISIBLE else View.GONE
        balanceViewPager.visibility = contentVisibility
        indicator.visibility = contentVisibility
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
           // excludeChildren(receivingCurrency, true)
           // excludeChildren(sendingCurrency, true)
        })
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

        val visibility = if (isEnable) View.GONE else View.VISIBLE

        btnExpandAvailable.visibility = visibility
        btnExpandInProgress.visibility = visibility

        clearTitleListeners()
        addTitleListeners(isEnable)

        if (!isEnable) {
            presenter?.onCheckShouldExpandAvailable()
            presenter?.onCheckShouldExpandInProgress()
        }
    }

    override fun showFaucet(show: Boolean) {
        faucetLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showSecure(show: Boolean) {
        secureLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showReceiveFaucet() {
        val allow = PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)

        if (allow) {
            presenter?.generateFaucetAddress()
        }
        else{
            showAlert(
                    getString(R.string.common_external_link_dialog_message),
                    getString(R.string.open),
                    {  presenter?.generateFaucetAddress() },
                    getString(R.string.common_external_link_dialog_title),
                    getString(R.string.cancel)
            )
        }
    }

    override fun onFaucetAddressGenerated(link: String) {
        openExternalLink(link)
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

    override fun showSeedScreen() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToDoubleAuthorizationFragment(DoubleAuthorizationFragment.Mode.VerificationSeed))
    }

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnNext.setOnClickListener(null)
        btnExpandAvailable.setOnClickListener(null)
        btnExpandInProgress.setOnClickListener(null)
        availableTitleContainer.setOnClickListener(null)
        inProgressTitleContainer.setOnClickListener(null)
        availableTitle.setOnClickListener(null)
        maturingTitle.setOnClickListener(null)
        btnShowAll.setOnClickListener(null)
        btnFaucetClose.setOnClickListener(null)
        btnSecureClose.setOnClickListener(null)
        btnFaucetReceive.setOnClickListener(null)
        btnSecureReceive.setOnClickListener(null)
        balanceViewPager.removeOnPageChangeListener(onPageSelectedListener)
        clearTitleListeners()
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun closeDrawer() {
    }

    override fun onStart() {
        super.onStart()

        App.showNotification = false

        onBackPressedCallback.isEnabled = true

        (activity as? AppActivity)?.checkShortCut()
    }

    override fun onStop() {
        App.showNotification = true

        onBackPressedCallback.isEnabled = false

        super.onStop()
    }


    override fun clearAllNotification() {
        (context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancelAll()
    }

    override fun selectWalletMenu() {
        (activity as? AppActivity)?.selectItem(NavItem.ID.WALLET)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return WalletPresenter(this, WalletRepository(), WalletState())
    }
}
