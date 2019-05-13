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