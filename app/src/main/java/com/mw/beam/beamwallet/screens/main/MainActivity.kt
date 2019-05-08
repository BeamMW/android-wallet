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

package com.mw.beam.beamwallet.screens.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.utils.LogUtils
import com.mw.beam.beamwallet.screens.address_details.AddressActivity
import com.mw.beam.beamwallet.screens.addresses.AddressesFragment
import com.mw.beam.beamwallet.screens.change_password.ChangePassActivity
import com.mw.beam.beamwallet.screens.receive.ReceiveActivity
import com.mw.beam.beamwallet.screens.send.SendActivity
import com.mw.beam.beamwallet.screens.settings.SettingsFragment
import com.mw.beam.beamwallet.screens.transaction_details.TransactionDetailsActivity
import com.mw.beam.beamwallet.screens.utxo.UtxoFragment
import com.mw.beam.beamwallet.screens.utxo_details.UtxoDetailsActivity
import com.mw.beam.beamwallet.screens.wallet.WalletFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainActivity : BaseActivity<MainPresenter>(), MainContract.View,
        WalletFragment.WalletHandler,
        UtxoFragment.UtxoDetailsHandler,
        AddressesFragment.AddressDetailsHandler,
        SettingsFragment.SettingsHandler {
    private lateinit var presenter: MainPresenter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navItemsAdapter: NavItemsAdapter

    override fun onControllerGetContentLayoutId() = R.layout.activity_main
    override fun getToolbarTitle(): String? = null
    override fun onShowTransactionDetails(item: TxDescription) = presenter.onShowTransactionDetails(item)
    override fun onReceive() = presenter.onReceive()
    override fun onSend() = presenter.onSend()
    override fun onChangePassword() = presenter.onChangePass()

    override fun configNavDrawer() {
        val toolbar = toolbarLayout.toolbar
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.common_drawer_open, R.string.common_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        configNavView()
    }

    override fun addListeners() {
        whereBuyBeamLink.setOnClickListener {
            presenter.onWhereBuyBeamPressed()
        }
    }

    override fun showTransactionDetails(item: TxDescription) {
        startActivity(Intent(this, TransactionDetailsActivity::class.java)
                .putExtra(TransactionDetailsActivity.EXTRA_TRANSACTION_DETAILS, item))
    }

    override fun onShowUtxoDetails(item: Utxo) {
        startActivity(Intent(this, UtxoDetailsActivity::class.java)
                .putExtra(UtxoDetailsActivity.EXTRA_UTXO, item))
    }

    override fun onShowAddressDetails(item: WalletAddress) {
        startActivity(Intent(this, AddressActivity::class.java)
                .putExtra(AddressActivity.EXTRA_ADDRESS, item))
    }

    override fun showReceiveScreen() = startActivity(Intent(this, ReceiveActivity::class.java))
    override fun showSendScreen() = startActivity(Intent(this, SendActivity::class.java))
    override fun showChangePasswordScreen() = startActivity(Intent(this, ChangePassActivity::class.java))

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        if (supportFragmentManager.backStackEntryCount == 1) {
            presenter.onClose()
            finish()
            return
        }

        super.onBackPressed()

        when (supportFragmentManager.findFragmentById(R.id.container)?.tag) {
            WalletFragment.getFragmentTag() -> navItemsAdapter.selectItem(NavItem.ID.WALLET)
            UtxoFragment.getFragmentTag() -> navItemsAdapter.selectItem(NavItem.ID.UTXO)
            SettingsFragment.getFragmentTag() -> navItemsAdapter.selectItem(NavItem.ID.SETTINGS)
            AddressesFragment.getFragmentTag() -> navItemsAdapter.selectItem(NavItem.ID.ADDRESS_BOOK)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun showOpenLinkAlert() {
        showAlert(
                getString(R.string.common_external_link_dialog_message),
                getString(R.string.common_drawer_open),
                { presenter.onOpenLinkPressed() },
                getString(R.string.common_external_link_dialog_title),
                getString(R.string.common_cancel)
        )
    }

    private fun configNavView() {
        val menuItems = arrayOf(
                NavItem(NavItem.ID.WALLET, R.drawable.menu_wallet_active, getString(R.string.nav_wallet), isSelected = true),
                NavItem(NavItem.ID.ADDRESS_BOOK, R.drawable.menu_address_book, getString(R.string.nav_address_book)),
                NavItem(NavItem.ID.UTXO, R.drawable.menu_utxo, getString(R.string.nav_utxo)),
                NavItem(NavItem.ID.SETTINGS, R.drawable.menu_settings, getString(R.string.nav_settings)))

        navItemsAdapter = NavItemsAdapter(this, menuItems, object : NavItemsAdapter.OnItemClickListener {
            override fun onItemClick(navItem: NavItem) {
                drawerLayout.closeDrawer(GravityCompat.START)

                //TODO presenter?
                when (navItem.id) {
                    NavItem.ID.WALLET -> showFragment(WalletFragment.newInstance(), WalletFragment.getFragmentTag(), WalletFragment.getFragmentTag(), true)
                    NavItem.ID.ADDRESS_BOOK -> showFragment(AddressesFragment.newInstance(), AddressesFragment.getFragmentTag(), AddressesFragment.getFragmentTag(), true)
                    NavItem.ID.UTXO -> showFragment(UtxoFragment.newInstance(), UtxoFragment.getFragmentTag(), UtxoFragment.getFragmentTag(), true)
                    NavItem.ID.DASHBOARD -> LogUtils.log("dashboard")
                    NavItem.ID.NOTIFICATIONS -> LogUtils.log("notifications")
                    NavItem.ID.HELP -> LogUtils.log("help")
                    NavItem.ID.SETTINGS -> showFragment(SettingsFragment.newInstance(), SettingsFragment.getFragmentTag(), SettingsFragment.getFragmentTag(), true)
                }
            }
        })
        navMenu.layoutManager = LinearLayoutManager(this)
        navMenu.adapter = navItemsAdapter

        // handler is needed to make it work somehow
        Handler().postDelayed({
            run {
                navMenu.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
            }
        }, 100)
    }

    override fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun clearListeners() {
        drawerLayout.removeDrawerListener(drawerToggle)
        whereBuyBeamLink.setOnClickListener(null)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = MainPresenter(this, MainRepository())
        return presenter
    }
}
