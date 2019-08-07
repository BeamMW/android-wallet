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

package com.mw.beam.beamwallet.core.helpers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mw.beam.beamwallet.R
import java.util.*

object CategoryHelper {
    private val gson = Gson()
    private val categoryData: CategoryData by lazy {
        val json = PreferencesManager.getString(PreferencesManager.KEY_CATEGORY_DATA)

        if (json.isNullOrBlank()) {
            return@lazy CategoryData(listOf())
        }

        gson.fromJson(json, CategoryData::class.java)
    }
    val noneCategory: Category by lazy {
        Category("none")
    }

    private fun saveCategoryData() {
        PreferencesManager.putString(PreferencesManager.KEY_CATEGORY_DATA, gson.toJson(categoryData))
    }

    fun getAllCategory() = categoryData.getAllCategory().values.toList()

    fun getCategory(categoryId: String) = categoryData.getAllCategory().values.firstOrNull { it.id == categoryId }

    fun saveCategory(category: Category) {
        categoryData.saveCategory(category)
        saveCategoryData()
    }

    fun getCategoryForAddress(address: String): Category? {
        return categoryData.getAllCategory().values.firstOrNull { it.addresses.contains(address) }
    }

    fun changeCategoryForAddress(address: String, category: Category?) {
        if (category == null) {
            removeAddressFromAllCategory(address)
        } else {
            categoryData.getAllCategory().values.forEach {
                if (it.id == category.id) {
                    it.addAddress(address)
                } else {
                    it.removeAddress(address)
                }
            }
        }
        saveCategoryData()
    }

//    fun getCategoryForAddress(address: String): List<Category> {
//        return categoryData.getAllCategory().values.filter { it.addresses.contains(address) }
//    }
//
//    fun changeCategoryForAddress(address: String, categoryList: List<Category>?) {
//        if (categoryList == null) {
//            removeAddressFromAllCategory(address)
//        } else {
//            categoryData.getAllCategory().values.forEach {
//                if (categoryList.any { category -> category.id == it.id }) {
//                    it.addAddress(address)
//                } else {
//                    it.removeAddress(address)
//                }
//            }
//        }
//        saveCategoryData()
//    }

    private fun removeAddressFromAllCategory(address: String) {
        categoryData.getAllCategory().values.forEach { it.removeAddress(address) }
    }

    fun deleteCategory(category: Category) {
        categoryData.removeCategory(category)
        saveCategoryData()
    }
}

private data class CategoryData(@SerializedName("data") var data: List<Category>) {
    fun getAllCategory() = HashMap(data.map { it.id to it }.toMap())

    fun saveCategory(category: Category) {
        data = getAllCategory().apply {
            put(category.id, category)
        }.values.toList()
    }

    fun removeCategory(category: Category) {
        data = getAllCategory().apply {
            remove(category.id)
        }.values.toList()
    }
}

data class Category(
        @SerializedName("id") val id: String,
        @SerializedName("color") var color: CategoryColor = CategoryColor.Red,
        @SerializedName("name") var name: String = "",
        @SerializedName("addresses") var addresses: List<String> = listOf()) {

    companion object {
        fun new(): Category {
            return Category(UUID.randomUUID().toString(), color = CategoryColor.values().random())
        }
    }

    fun addAddress(address: String) {
        addresses = addresses.toHashSet().apply {
            add(address)
        }.toList()
    }

    fun removeAddress(address: String) {
        addresses = addresses.toHashSet().apply {
            remove(address)
        }.toList()
    }
}

enum class CategoryColor {
    Red, Orange, Yellow, Green, Blue, Pink;

    fun getAndroidColorId() = when(this) {
        Red -> R.color.category_red
        Orange -> R.color.category_orange
        Yellow -> R.color.category_yellow
        Green -> R.color.category_green
        Blue -> R.color.category_blue
        Pink -> R.color.category_pink
    }
}