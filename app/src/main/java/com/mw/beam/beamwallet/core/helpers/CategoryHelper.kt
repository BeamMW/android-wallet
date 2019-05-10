package com.mw.beam.beamwallet.core.helpers

import android.support.v4.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import java.util.*

object CategoryHelper {
    private lateinit var gson: Gson
    private val categoryData: CategoryData by lazy {
        val json = PreferencesManager.getString(PreferencesManager.KEY_CATEGORY_DATA)

        if (json.isNullOrBlank()) {
            return@lazy CategoryData(listOf())
        }

        gson = Gson()

        gson.fromJson(json, CategoryData::class.java)
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

    fun getCategoryFromAddress(address: String): Category? {
        return categoryData.getAllCategory().values.firstOrNull { it.addresses.contains(address) }
    }

    fun changeAddressCategoryTo(address: String, category: Category?) {
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
            return Category(UUID.randomUUID().toString())
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
        CategoryColor.Red -> R.color.category_red
        CategoryColor.Orange -> R.color.category_orange
        CategoryColor.Yellow -> R.color.category_yellow
        CategoryColor.Green -> R.color.category_green
        CategoryColor.Blue -> R.color.category_blue
        CategoryColor.Pink -> R.color.category_pink
    }
}