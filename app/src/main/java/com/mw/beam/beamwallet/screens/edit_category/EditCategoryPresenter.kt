package com.mw.beam.beamwallet.screens.edit_category

import com.mw.beam.beamwallet.base_screen.BasePresenter

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