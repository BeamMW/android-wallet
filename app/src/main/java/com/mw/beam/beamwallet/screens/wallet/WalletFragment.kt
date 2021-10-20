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
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.widget.PopupMenu

import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.toolbar.*

import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.confirm.DoubleAuthorizationFragmentMode
import com.mw.beam.beamwallet.core.helpers.NetworkStatus
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.views.gone
import com.mw.beam.beamwallet.screens.timer_overlay_dialog.TimerOverlayDialog
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.*
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.entities.TxDescription


class WalletFragment : BaseFragment<WalletPresenter>(), WalletContract.View {
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var assetsAdapter: AssetsAdapter

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

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color_black) }
    else{
        ContextCompat.getColor(requireContext(), R.color.addresses_status_bar_color)
    }
    override fun onControllerGetContentLayoutId() = R.layout.fragment_wallet
    override fun getToolbarTitle(): String = getString(R.string.wallet)

    override fun configWalletStatus(assets: List<Asset>) {
        if (assets.count() > 1) {
            assetsHeader.visibility = View.VISIBLE
        }
        else {
            assetsHeader.visibility = View.INVISIBLE
        }
        assetsAdapter.reloadData(assets)
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        transactionsList.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        emptyTransactionsListMessage.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        transactionsHeader.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

        if (transactions.isNotEmpty()) {
            transactionsAdapter.data = transactions
            transactionsAdapter.notifyDataSetChanged()

            btnShowAll.text = getString(R.string.show_all)
            btnShowAll.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null)
        }
    }

    override fun showAllTransactions() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToTransactionsFragment())
    }

    override fun showAllAssets() {
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToAssetsListFragment())
    }

    override fun init() {
        initTransactionsList()
        setHasOptionsMenu(true)

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), onBackPressedCallback)

        DAOManager.loadApps(requireContext())

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        itemsswipetorefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        itemsswipetorefresh.setOnRefreshListener {
            AppManager.instance.reload()
            android.os.Handler().postDelayed({
                if (itemsswipetorefresh!=null) {
                    itemsswipetorefresh.isRefreshing = false
                }
            }, 1000)
        }

        if(!PreferencesManager.getBoolean(PreferencesManager.KEY_BACKGROUND_MODE_ASK,false)) {
            android.os.Handler().postDelayed({
                showAlert(message = getString(R.string.background_mode_text),
                        btnConfirmText = getString(R.string.allow),
                        onConfirm = {
                            PreferencesManager.putBoolean(PreferencesManager.KEY_BACKGROUND_MODE, true)
                            PreferencesManager.putBoolean(PreferencesManager.KEY_BACKGROUND_MODE_ASK, true)
                            App.self.startBackgroundService()
                        },
                        onCancel = {
                            PreferencesManager.putBoolean(PreferencesManager.KEY_BACKGROUND_MODE, false)
                            PreferencesManager.putBoolean(PreferencesManager.KEY_BACKGROUND_MODE_ASK, true)
                        },
                        title = getString(R.string.background_mode_title),
                        btnCancelText = getString(R.string.cancel))
            }, 500)
        }


    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
        btnReceive.setOnClickListener {
            presenter?.onReceivePressed()
        }
        btnNext.setOnClickListener {
            AssetManager.instance.selectedAssetId = 0
            presenter?.onSendPressed()
        }

        btnShowAll.setOnClickListener {
            val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)

            if (AppManager.instance.getTransactions().count() > 0) {
                presenter?.onShowAllPressed()
            }
            else PopupMenu(wrapper, btnShowAll).apply {
                setOnMenuItemClickListener {
                    findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToProofVerificationFragment())
                    true
                }
                inflate(R.menu.proof_menu)
                show()
            }
        }

        btnShowAllAssets.setOnClickListener {
            if (assetsHeader.visibility == View.VISIBLE) {
                presenter?.onShowAllAssetsPressed()
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

    private fun initTransactionsList() {
        val context = context ?: return

        transactionsAdapter = TransactionsAdapter(context, null, mutableListOf(), TransactionsAdapter.Mode.SHORT) {
            presenter?.onTransactionPressed(it)
        }

        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = transactionsAdapter

        assetsAdapter = AssetsAdapter(
                context, presenter?.state?.getAssets() ?: arrayListOf()
        ) {
            findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToAssetDetailFragment(it.assetId, it.unitName))
        }

        assetsList.layoutManager = LinearLayoutManager(context)
        assetsList.adapter = assetsAdapter
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
        ExchangeManager.instance.isPrivacyMode = isEnable

        activity?.invalidateOptionsMenu()

        transactionsAdapter.setPrivacyMode(isEnable)
        assetsAdapter.notifyDataSetChanged()
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
        blurView.visibility = View.VISIBLE

        jp.wasabeef.blurry.Blurry.with(context).capture(view).into(blurView)

        val dialog = TimerOverlayDialog.newInstance {
            blurView.visibility = View.GONE
            if(it) {
                openExternalLink(link)
            }
        }
        dialog.show(activity?.supportFragmentManager!!, TimerOverlayDialog.getFragmentTag())
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
        findNavController().navigate(WalletFragmentDirections.actionWalletFragmentToDoubleAuthorizationFragment(DoubleAuthorizationFragmentMode.VerificationSeed))
    }

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnNext.setOnClickListener(null)
        btnShowAll.setOnClickListener(null)
        btnFaucetClose.setOnClickListener(null)
        btnSecureClose.setOnClickListener(null)
        btnFaucetReceive.setOnClickListener(null)
        btnSecureReceive.setOnClickListener(null)
        btnShowAllAssets.setOnClickListener(null)
    }


    override fun closeDrawer() {
    }

    override fun onStart() {
        super.onStart()

        onBackPressedCallback.isEnabled = true

        (activity as? AppActivity)?.checkShortCut()

        if(AppManager.instance.getNetworkStatus() == NetworkStatus.OFFLINE) {
            AppManager.instance.setNetworkStatus(false)
        }

        AppManager.instance.wallet?.getTransactions()
    }

    override fun onStop() {

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
