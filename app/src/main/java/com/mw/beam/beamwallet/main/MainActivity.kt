package com.mw.beam.beamwallet.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.utils.LogUtils
import com.mw.beam.beamwallet.receive.ReceiveActivity
import com.mw.beam.beamwallet.send.SendActivity
import com.mw.beam.beamwallet.transactionDetails.TransactionDetailsActivity
import com.mw.beam.beamwallet.wallet.WalletFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainActivity : BaseActivity<MainPresenter>(), MainContract.View, WalletFragment.TransactionDetailsHandler {
    private lateinit var presenter: MainPresenter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onControllerGetContentLayoutId() = R.layout.activity_main
    override fun onShowTransactionDetails(item: TxDescription) = presenter.onShowTransactionDetails(item)
    override fun onReceive() = presenter.onReceive()
    override fun onSend() = presenter.onSend()

    override fun configNavDrawer() {
        val toolbar = toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.common_drawer_open, R.string.common_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        configNavView()
    }

    override fun showTransactionDetails(item: TxDescription) {
        startActivity(Intent(this, TransactionDetailsActivity::class.java)
                .putExtra(TransactionDetailsActivity.EXTRA_TRANSACTION_DETAILS, item))
    }

    override fun showReceiveScreen() {
        startActivity(Intent(this, ReceiveActivity::class.java))
    }

    override fun showSendScreen() {
        startActivity(Intent(this, SendActivity::class.java))
    }

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

    private fun configNavView() {
        getColorStateList(R.color.menu_selector).apply {
            navView.itemIconTintList = this
            navView.itemTextColor = this
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_wallet -> showFragment(WalletFragment.newInstance(), WalletFragment.getFragmentTag(), WalletFragment.getFragmentTag(), true)
                R.id.nav_address_book -> LogUtils.log("address_book")
                R.id.nav_utxo -> LogUtils.log("utxo")
                R.id.nav_dashboard -> LogUtils.log("dashboard")
                R.id.nav_notifications -> LogUtils.log("notifications")
                R.id.nav_help -> LogUtils.log("help")
                R.id.nav_settings -> LogUtils.log("settings")
            }
            true
        }

        navView.apply {
            setCheckedItem(R.id.nav_wallet)
            menu.performIdentifierAction(R.id.nav_wallet, 0)
        }
    }

    override fun clearListeners() {
        drawerLayout.removeDrawerListener(drawerToggle)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = MainPresenter(this, MainRepository())
        return presenter
    }
}
