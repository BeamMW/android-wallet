package com.mw.beam.beamwallet.screens.save_address

import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.views.CategorySpinner
import kotlinx.android.synthetic.main.fragment_save_address.*

class SaveAddressFragment: BaseFragment<SaveAddressPresenter>(), SaveAddressContract.View {
    override fun getAddress(): String = SaveAddressFragmentArgs.fromBundle(arguments!!).address

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_save_address

    override fun init(address: String, category: Category?) {
        this.address.text = address
        categorySpinner.selectCategory(category)
    }

    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        btnCancel.setOnClickListener {
            presenter?.onCancelPressed()
        }

        categorySpinner.setOnChangeCategoryListener(object : CategorySpinner.OnChangeCategoryListener {
            override fun onSelect(category: Category?) {
                presenter?.onSelectCategory(category)
            }

            override fun onAddNewCategoryPressed() {
                presenter?.onAddNewCategoryPressed()
            }
        })
    }

    override fun getName(): String {
        return name.text?.toString() ?: ""
    }

    override fun showAddNewCategory() {
        findNavController().navigate(SaveAddressFragmentDirections.actionSaveAddressFragmentToEditCategoryFragment())
    }

    override fun clearListeners() {
        btnSave.setOnClickListener(null)
        btnCancel.setOnClickListener(null)
        categorySpinner.setOnChangeCategoryListener(null)
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SaveAddressPresenter(this, SaveAddressRepository(), SaveAddressState())
    }

    override fun getToolbarTitle(): String? = getString(R.string.save_address)
}