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

package com.mw.beam.beamwallet.screens.edit_category

import androidx.recyclerview.widget.LinearLayoutManager
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
        colorListAdapter = ColorListAdapter {
            presenter?.onChangeColor(it)
        }

        colorList.adapter = colorListAdapter
        colorList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        }

        colorListAdapter?.setData(CategoryColor.values().asList())
        colorListAdapter?.setSelectedColor(category.color)
        btnSave.isEnabled = false
    }

    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        nameValue.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                presenter?.onNameChanged(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun setSaveEnabled(enable: Boolean) {
        btnSave.isEnabled = enable
    }

    override fun getName(): String {
        return nameValue.text.toString()
    }

    override fun getSelectedCategoryColor(): CategoryColor {
        return colorListAdapter?.getSelectedColor() ?: CategoryColor.Red
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return EditCategoryPresenter(this, EditCategoryRepository(), EditCategoryState())
    }
}