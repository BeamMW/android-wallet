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

import android.text.Editable
import android.text.TextWatcher
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagColor
import kotlinx.android.synthetic.main.fragment_edit_category.*
import java.util.*

class EditCategoryFragment: BaseFragment<EditCategoryPresenter>(), EditCategoryContract.View {
    private var colorListAdapter: ColorListAdapter? = null

    private val categoryIdFromArgument by lazy {
        if (arguments == null) {
            null
        } else {
            EditCategoryFragmentArgs.fromBundle(arguments!!).categoryId
        }
    }

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_edit_category

    override fun getToolbarTitle(): String? = if (categoryIdFromArgument == null) getString(R.string.create_tag) else getString(R.string.edit_tag)

    override fun getCategoryId(): String? {
        return categoryIdFromArgument
    }

    override fun init(tag: Tag) {
        nameValue.setText(tag.name)

        colorListAdapter = ColorListAdapter {
            presenter?.onChangeColor(it)
        }

        colorList.adapter = colorListAdapter
        colorList.layoutManager = LinearLayoutManager(context).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        }

        colorListAdapter?.setData(TagColor.values().asList())
        colorListAdapter?.setSelectedColor(tag.color)

        btnSave.isEnabled = false

        toolbarLayout.hasStatus = true
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
        if (btnSave!=null) {
            btnSave.isEnabled = enable
        }
    }

    override fun finish() {
        findNavController().popBackStack()
    }

    override fun getName(): String {
        return nameValue.text.toString()
    }

    override fun getSelectedCategoryColor(): TagColor {
        return colorListAdapter?.getSelectedColor() ?: TagColor.Red
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return EditCategoryPresenter(this, EditCategoryRepository(), EditCategoryState())
    }
}