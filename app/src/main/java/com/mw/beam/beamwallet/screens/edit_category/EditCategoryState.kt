package com.mw.beam.beamwallet.screens.edit_category

import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryColor

class EditCategoryState {
    var category: Category? = null
    var tempColor: CategoryColor = CategoryColor.Red
    var tempName: String = ""
}