package com.mw.beam.beamwallet.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.receive.ReceiveActivity
import com.mw.beam.beamwallet.send.SendActivity
import com.mw.beam.beamwallet.settings.SettingsFragment
import com.mw.beam.beamwallet.transactionDetails.TransactionDetailsActivity
import com.mw.beam.beamwallet.utxo.UtxoFragment
import com.mw.beam.beamwallet.utxoDetails.UtxoDetailsActivity
import com.mw.beam.beamwallet.wallet.WalletFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by vain onnellinen on 10/4/18.
 */
class MainActivity : BaseActivity<MainPresenter>(), MainContract.View, WalletFragment.TransactionDetailsHandler, UtxoFragment.UtxoDetailsHandler {
    private lateinit var presenter: MainPresenter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onControllerGetContentLayoutId() = R.layout.activity_main
    override fun getToolbarTitle(): String? = null
    override fun onShowTransactionDetails(item: TxDescription) = presenter.onShowTransactionDetails(item)
    override fun onReceive() = presenter.onReceive()
    override fun onSend() = presenter.onSend()

    override fun configNavDrawer() {
        val toolbar = toolbarLayout.toolbar
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

    override fun onShowUtxoDetails(item: Utxo, relatedTransactions: ArrayList<TxDescription>) {
        startActivity(Intent(this, UtxoDetailsActivity::class.java)
                .putExtra(UtxoDetailsActivity.EXTRA_UTXO_DETAILS, item)
                .putParcelableArrayListExtra(UtxoDetailsActivity.EXTRA_RELATED_TRANSACTIONS, relatedTransactions))
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

        //TODO presenter?
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_wallet -> showFragment(WalletFragment.newInstance(), WalletFragment.getFragmentTag(), WalletFragment.getFragmentTag(), true)
                //  R.id.nav_address_book -> LogUtils.log("address book")
                R.id.nav_utxo -> showFragment(UtxoFragment.newInstance(), UtxoFragment.getFragmentTag(), UtxoFragment.getFragmentTag(), true)
                // R.id.nav_dashboard -> LogUtils.log("dashboard")
                // R.id.nav_notifications -> LogUtils.log("notifications")
                //  R.id.nav_help -> LogUtils.log("help")
                R.id.nav_settings -> showFragment(SettingsFragment.newInstance(), SettingsFragment.getFragmentTag(), SettingsFragment.getFragmentTag(), true)
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

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = MainPresenter(this, MainRepository())
        return presenter
    }
}
