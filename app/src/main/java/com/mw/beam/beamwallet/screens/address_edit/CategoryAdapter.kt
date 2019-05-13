package com.mw.beam.beamwallet.screens.address_edit

import android.content.Context
import android.widget.ArrayAdapter
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.Category

class CategoryAdapter(context: Context, objects: MutableList<Category>) : ArrayAdapter<Category>(context, R.layout.spinner_item_category, objects) {

}