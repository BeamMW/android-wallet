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

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryColor

interface EditCategoryContract {
    interface View: MvpView {
        fun getCategoryId(): String?
        fun init(category: Category)
        fun getName(): String
        fun getSelectedCategoryColor(): CategoryColor
        fun setSaveEnabled(enable: Boolean)
        fun finish()
    }

    interface Presenter: MvpPresenter<View> {
        fun onSavePressed()
        fun onNameChanged(name: String)
        fun onChangeColor(categoryColor: CategoryColor)
    }

    interface Repository: MvpRepository {
        fun saveCategory(category: Category)
        fun getCategoryFromId(categoryId: String): Category?
        fun createNewCategory(): Category
    }
}