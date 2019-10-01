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

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.screens.addresses.AddressesAdapter
import kotlinx.android.synthetic.main.fragment_category.*
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.AbsoluteSizeSpan



class CategoryFragment : BaseFragment<CategoryPresenter>(), CategoryContract.View {

    private lateinit var pagerAdapter: CategoriesPagerAdapter

    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun getToolbarTitle(): String? = getString(R.string.tag)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_category

    override fun getCategoryId(): String {
        return CategoryFragmentArgs.fromBundle(arguments!!).categoryId
    }

    override fun init(tag: Tag) {
        toolbarLayout.hasStatus = true

        nameValue.text = tag.name
        nameValue.setTextColor(resources.getColor(tag.color.getAndroidColorId(), context?.theme))

        setHasOptionsMenu(true)

        val context = context ?: return
        pagerAdapter = CategoriesPagerAdapter(context, tag.id,
                object : AddressesAdapter.OnItemClickListener {
                    override fun onItemClick(item: WalletAddress) {
                        presenter?.onAddressPressed(item)
                    }
                })

        pager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(pager)
        tabLayout.setSelectedTabIndicatorColor(resources.getColor(tag.color.getAndroidColorId(), context?.theme))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.category_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> presenter?.onDeleteCategoryPressed()
            R.id.edit -> presenter?.onEditCategoryPressed()
        }

        return true
    }

    @SuppressLint("StringFormatInvalid")
    override fun showConfirmDeleteDialog(categoryName: String) {
        val sp = 15f
        val px = sp * resources.displayMetrics.scaledDensity

        val string = getString(R.string.tag_delete_dialog_message, categoryName)

        val spannable = SpannableString(string)
        val indexStart = string.indexOf(categoryName)
        val indexEnd = indexStart + categoryName.length
        spannable.setSpan(StyleSpan(Typeface.BOLD), indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(AbsoluteSizeSpan(px.toInt()), indexStart, indexEnd, 0);

        showAlert(
                spannable,
                getString(R.string.delete),
                { presenter?.onDeleteCategoryConfirmed() },
                getString(R.string.delete_tag),
                getString(R.string.cancel),
                {})
    }

    override fun showAddressDetails(address: WalletAddress) {
        findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToAddressFragment(address))
    }

    override fun updateAddresses(tab: Tab, addresses: List<WalletAddress>) {
        pagerAdapter?.setData(tab,addresses)
    }

    override fun displayTabs(display: Boolean) {
        if (!display) {
            tabLayout.visibility = View.GONE
            pager.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        }
    }

    override fun navigateToEditCategory(categoryId: String) {
        findNavController().navigate(CategoryFragmentDirections.actionCategoryFragmentToEditCategoryFragment(categoryId))
    }

    override fun finish() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return CategoryPresenter(this, CategoryRepository(), CategoryState())
    }

}