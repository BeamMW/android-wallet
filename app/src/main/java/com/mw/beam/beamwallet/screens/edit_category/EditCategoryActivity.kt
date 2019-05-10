package com.mw.beam.beamwallet.screens.edit_category

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseActivity
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryColor
import kotlinx.android.synthetic.main.activity_edit_category.*

class EditCategoryActivity: BaseActivity<EditCategoryPresenter>(), EditCategoryContract.View {
    private lateinit var presenter: EditCategoryPresenter
    private var colorListAdapter: ColorListAdapter? = null
    companion object {
        const val CATEGORY_ID_KEY = "CATEGORY_ID_KEY"
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.activity_edit_category


    override fun getToolbarTitle(): String? = getString(R.string.edit_category_toolbar_title)

    override fun getCategoryId(): String? {
        return intent?.extras?.getString(EditCategoryActivity.CATEGORY_ID_KEY, null)
    }

    override fun init(category: Category) {
        nameValue.setText(category.name)
        colorListAdapter = ColorListAdapter()
        colorList.adapter = colorListAdapter
    }

    override fun getName(): String {
        return nameValue.text.toString()
    }

    override fun getSelectedCategoryColor(): CategoryColor {
        return colorListAdapter?.getSelectedColor() ?: CategoryColor.Red
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        presenter = EditCategoryPresenter(this, EditCategoryRepository(), EditCategoryState())
        return presenter
    }
}