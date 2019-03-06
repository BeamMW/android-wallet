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

package com.mw.beam.beamwallet.screens.address_details

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.utils.CalendarUtils
import com.mw.beam.beamwallet.screens.address_edit.EditAddressActivity
import com.mw.beam.beamwallet.screens.transaction_details.TransactionDetailsActivity
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.item_address.*

/**
 * Created by vain onnellinen on 3/4/19.
 */
class AddressActivity : BaseActivity<AddressPresenter>(), AddressContract.View {
    private lateinit var presenter: AddressPresenter
    private lateinit var adapter: TransactionsAdapter

    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        const val CODE_EDIT_ADDRESS = 1
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_address
    override fun getToolbarTitle(): String? = getString(R.string.address_title)
    override fun getAddress(): WalletAddress = intent.getParcelableExtra(EXTRA_ADDRESS)

    override fun init(address: WalletAddress) {
        configAddressDetails(address)
        initTransactionsList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.address_menu, menu)
        presenter.onMenuCreate(menu)

        return true
    }

    override fun configMenuItems(menu: Menu?, address: WalletAddress) {
        menu?.findItem(R.id.edit)?.isVisible = address.own != 0L
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            //   R.id.showQR -> presenter.onShowQR()
            R.id.edit -> presenter.onEditAddress()
            R.id.delete -> presenter.onDeleteAddress()
        }

        return true
    }

    private fun initTransactionsList() {
        adapter = TransactionsAdapter(this, mutableListOf(), object : TransactionsAdapter.OnItemClickListener {
            override fun onItemClick(item: TxDescription) {
                presenter.onTransactionPressed(item)
            }
        })

        transactionsList.layoutManager = LinearLayoutManager(this)
        transactionsList.adapter = adapter
    }

    private fun configAddressDetails(address: WalletAddress) {
        label.text = address.label
        id.text = address.walletID
        if (!address.isContact) {
            date.text = String.format(if (address.isExpired) getString(R.string.addresses_expired) else getString(R.string.addresses_expires),
                    if (address.duration == 0L) getString(R.string.addresses_never) else CalendarUtils.fromTimestamp(address.createTime + address.duration))
        }
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        transactionsTitle.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE

        if (transactions.isNotEmpty()) {
            adapter.setData(transactions)
        }
    }

    override fun showTransactionDetails(txDescription: TxDescription) {
        startActivity(Intent(this, TransactionDetailsActivity::class.java)
                .putExtra(TransactionDetailsActivity.EXTRA_TRANSACTION_DETAILS, txDescription))
    }

    override fun showEditAddressScreen(address: WalletAddress) {
        startActivityForResult(Intent(this, EditAddressActivity::class.java)
                .putExtra(EditAddressActivity.EXTRA_ADDRESS_FOR_EDIT, address), CODE_EDIT_ADDRESS)
    }

    override fun finishScreen() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_EDIT_ADDRESS && resultCode == RESULT_OK) {
            presenter.onAddressWasEdited()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = AddressPresenter(this, AddressRepository(), AddressState())
        return presenter
    }
}
