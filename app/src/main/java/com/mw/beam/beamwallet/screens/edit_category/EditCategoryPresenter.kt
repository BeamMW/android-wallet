package com.mw.beam.beamwallet.screens.edit_category

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.CategoryColor

class EditCategoryPresenter(view: EditCategoryContract.View?, repository: EditCategoryContract.Repository, private val state: EditCategoryState)
    : BasePresenter<EditCategoryContract.View, EditCategoryContract.Repository>(view, repository), EditCategoryContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        val id = view?.getCategoryId()
        var category = if (id == null) repository.createNewCategory() else repository.getCategoryFromId(id)

        if (category == null) {
            category = repository.createNewCategory()
        }

        state.category = category

        view?.init(category)
    }

    override fun onChangeColor(categoryColor: CategoryColor) {
        state.tempColor = categoryColor
        checkSaveButtonEnabled()
    }

    override fun onNameChanged(name: String) {
        state.tempName = name
        checkSaveButtonEnabled()
    }

    fun checkSaveButtonEnabled() {
        val isNameChanged = state.category?.name != state.tempName && state.tempName.isNotBlank()
        val isColorChanged = state.category?.color != state.tempColor
        view?.setSaveEnabled(isNameChanged || isColorChanged)
    }

    override fun onSavePressed() {
        if (view == null) {
            return
        }

        state.category?.let {
            it.name = view!!.getName().trim()
            it.color = view!!.getSelectedCategoryColor()

            repository.saveCategory(it)
        }

        view?.finish()
    }
}