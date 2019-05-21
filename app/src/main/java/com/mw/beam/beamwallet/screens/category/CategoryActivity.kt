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

package com.mw.beam.beamwallet.screens.category

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.screens.address_details.AddressActivity
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter
import com.mw.beam.beamwallet.screens.edit_category.EditCategoryActivity
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : BaseActivity<CategoryPresenter>(), CategoryContract.View {
    private var addressesAdapter: AddressesAdapter? = null

    companion object {
        const val CATEGORY_ID_KEY = "CATEGORY_ID_KEY"
    }

    override fun getToolbarTitle(): String? = getString(R.string.category_toolbar_title)

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_category

    override fun getCategoryId(): String {
        return intent?.extras?.getString(CATEGORY_ID_KEY) ?: ""
    }

    override fun init(category: Category) {
        nameValue.text = category.name
        nameValue.setTextColor(resources.getColor(category.color.getAndroidColorId(), theme))

        addressesAdapter = AddressesAdapter(this, object : AddressesAdapter.OnItemClickListener {
            override fun onItemClick(item: WalletAddress) {
                presenter?.onAddressPressed(item)
            }
        })

        addressesRecyclerView.adapter = addressesAdapter
        addressesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.delete -> presenter?.onDeleteCategoryPressed()
            R.id.edit -> presenter?.onEditCategoryPressed()
        }

        return true
    }

    override fun showConfirmDeleteDialog(categoryName: String) {
        showAlert(
                getString(R.string.category_delete_dialog_message, categoryName),
                getString(R.string.common_cancel),
                {},
                getString(R.string.category_delete_dialog_title),
                getString(R.string.common_delete),
                { presenter?.onDeleteCategoryConfirmed() })
    }

    override fun showAddressDetails(address: WalletAddress) {
        startActivity(Intent(this, AddressActivity::class.java)
                .putExtra(AddressActivity.EXTRA_ADDRESS, address))
    }

    override fun updateAddresses(addresses: List<WalletAddress>) {
        addressesAdapter?.setData(addresses)
    }

    override fun navigateToEditCategory(categoryId: String) {
        startActivity(Intent(this, EditCategoryActivity::class.java).apply { putExtra(EditCategoryActivity.CATEGORY_ID_KEY, categoryId) })
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return CategoryPresenter(this, CategoryRepository(), CategoryState())
    }

}