package com.mw.beam.beamwallet.screens.category

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter
import com.mw.beam.beamwallet.screens.edit_category.EditCategoryActivity
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity: BaseActivity<CategoryPresenter>(), CategoryContract.View {
    private lateinit var presenter: CategoryPresenter
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

            }
        })

        addressesRecyclerView.adapter = addressesAdapter
        addressesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun updateAddresses(addresses: List<WalletAddress>) {
        addressesAdapter?.setData(addresses)
    }

    override fun navigateToEditCategory(categoryId: String) {
        startActivity(Intent(this, EditCategoryActivity::class.java).apply { putExtra(EditCategoryActivity.CATEGORY_ID_KEY, categoryId) })
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = CategoryPresenter(this, CategoryRepository(), CategoryState())
        return presenter
    }

}