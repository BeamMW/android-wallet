package com.mw.beam.beamwallet.screens.edit_category

import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
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
        colorList.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        colorListAdapter?.setData(CategoryColor.values().asList())
        btnSave.isEnabled = false
    }

    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter.onSavePressed()
        }

        nameValue.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = !getName().isBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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