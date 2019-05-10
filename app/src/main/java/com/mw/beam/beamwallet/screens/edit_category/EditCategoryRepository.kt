package com.mw.beam.beamwallet.screens.edit_category

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper

class EditCategoryRepository: BaseRepository(), EditCategoryContract.Repository {
    override fun saveCategory(category: Category) {
        CategoryHelper.saveCategory(category)
    }

    override fun getCategoryFromId(categoryId: String): Category? {
        return CategoryHelper.getCategory(categoryId)
    }

    override fun createNewCategory(): Category {
        return Category.new()
    }
}