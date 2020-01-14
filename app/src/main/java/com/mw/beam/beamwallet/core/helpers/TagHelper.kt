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

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*
import java.util.regex.Pattern
import kotlin.Comparator
import kotlin.collections.HashMap

object TagHelper {
    var subOnCategoryCreated: Subject<Tag?> = PublishSubject.create<Tag?>().toSerialized()

    private val gson = Gson()
    private val tagData: TagData by lazy {
        val json = PreferencesManager.getString(PreferencesManager.KEY_TAG_DATA)

        if (json.isNullOrBlank()) {
            return@lazy TagData(listOf())
        }

        gson.fromJson(json, TagData::class.java)
    }

    private fun saveTagData() {
        PreferencesManager.putString(PreferencesManager.KEY_TAG_DATA, gson.toJson(tagData))
    }

    fun fixLegacyFormat() {
        if(!PreferencesManager.getBoolean(PreferencesManager.KEY_TAG_DATA_LEGACY,false)) {
            var tags = getAllTags().toMutableList()

            var fixedAddresses = HashMap<String, String>()

            for (tag in tags) {
                val ids = tag.addresses
                tag.id = (1..100000).random().toString()

                for (id in ids) {
                    var address = AppManager.instance.getAddress(id)
                    if (address != null) {
                        if (fixedAddresses[address.walletID] == null) {
                            fixedAddresses[address.walletID] = tag.id
                        } else {
                            var categories = fixedAddresses[address.walletID]?.split(";")?.toMutableList()
                            if (categories != null) {
                                categories.add(tag.id)
                                fixedAddresses[address.walletID] = categories.joinToString(";")
                            }
                        }
                    }
                }
            }

            for (key in fixedAddresses.keys) {
                var categories = fixedAddresses[key]?.split(";")?.toMutableList()
                var address = AppManager.instance.getAddress(key)

                if (categories != null && address != null) {
                    var dto = address.toDTO()
                    dto.category = fixedAddresses[key] as String
                    AppManager.instance.wallet?.saveAddress(dto, address.isContact)
                }
            }

            tagData.clear()

            for (tag in tags) {
                tag.addresses = listOf()
                tagData.saveTag(tag)
            }

            saveTagData()

            PreferencesManager.putBoolean(PreferencesManager.KEY_TAG_DATA_LEGACY,true)
        }
    }

    fun getAllTags() = tagData.getAllTags().values.toList()

    fun getAllTagsSorted(): List<Tag> {
        val tags: List<Tag> = getAllTags()
        val pattern = Pattern.compile("^\\d+")
        val comparator = object : Comparator<Tag> {
            override
            fun compare(object1: Tag, object2: Tag): Int {
                var m = pattern.matcher(object1.name.toLowerCase())
                var number1: Int?
                if (!m.find()) {
                    return object1.name.toLowerCase().compareTo(object2.name.toLowerCase())
                } else {
                    var number2: Int?
                    number1 = Integer.parseInt(m.group())
                    m = pattern.matcher(object2.name.toLowerCase())
                    return if (!m.find()) {
                        object1.name.toLowerCase().compareTo(object2.name.toLowerCase())
                    } else {
                        number2 = Integer.parseInt(m.group())
                        val comparison = number1.compareTo(number2)
                        if (comparison != 0) {
                            comparison
                        } else {
                            object1.compareTo(object2)
                        }
                    }
                }
            }
        }
        Collections.sort(tags, comparator)
        return tags
    }

    fun getTag(tagId: String) = tagData.getAllTags().values.firstOrNull { it.id == tagId }

    fun saveTag(tag: Tag) {
        tagData.saveTag(tag)

        saveTagData()

        subOnCategoryCreated.onNext(tag)
    }

    fun getAddressesForTag(tagID: String?): List<WalletAddress> {
        var result = mutableListOf<WalletAddress>()

       if(tagID!=null) {
           for (address in AppManager.instance.getAllAddresses()) {
               var categories = address.splitCategories()
               if(categories.contains(tagID)) {
                   result.add(address)
               }
           }
       }

        return result
    }

    fun getTagsForAddress(addressID: String): List<Tag> {
        var address = AppManager.instance.getAddress(addressID)
        if (address!=null) {
            var categories = address.splitCategories()
            if (categories.count() > 0) {
                var result = mutableListOf<Tag>()

                for (tag in getAllTags()) {
                    for (id in categories) {
                        if(tag.id == id) {
                            result.add(tag);
                        }
                    }
                }
                return result
            }
        }

        return listOf()
    }

    fun changeTagsForAddress(id: String, tagList: List<Tag>?) {
        if (tagList.isNullOrEmpty()) {
            removeAddressFromAllTags(id)
        } else {
            var address = AppManager.instance.getAddress(id)

            if(address!=null)
            {
                var categories = mutableListOf<String>()

                for (t in tagList) {
                    categories.add(t.id)
                }

                var ids = categories.joinToString(";")

                var dto = address.toDTO()
                dto.category = ids
                AppManager.instance.wallet?.saveAddress(dto, address.isContact)
            }
        }
        saveTagData()
    }

    private fun removeAddressFromAllTags(id: String) {
        var address = AppManager.instance.getAddress(id)
        if(address!=null) {
            var dto = address.toDTO()
            dto.category = ""
            AppManager.instance.wallet?.saveAddress(dto, address.isContact)
        }
    }

    fun deleteTag(tag: Tag) {
        var addresses = getAddressesForTag(tag.id)

        for (address in addresses) {
            var categories = address.splitCategories()
            categories.removeAll {
                it == tag.id
            }
            var ids = categories.joinToString(";")
            if(categories.count()==0) {
                ids = ""
            }

            var dto = address.toDTO()
            dto.category = ids
            AppManager.instance.wallet?.saveAddress(dto, address.isContact)
        }

        tagData.removeTag(tag)

        saveTagData()
    }
}
 private data class TagData(@SerializedName("data") var data: List<Tag>) {
    fun getAllTags() = HashMap(data.map { it.id to it }.toMap())

    fun saveTag(tag: Tag) {
        data = getAllTags().apply {
            put(tag.id, tag)
        }.values.toList()
    }

    fun removeTag(tag: Tag) {
        data = getAllTags().apply {
            remove(tag.id)
        }.values.toList()
    }

    fun clear() {
        data = listOf()
    }
}

data class Tag(
        @SerializedName("id") var id: String,
        @SerializedName("color") var color: TagColor = TagColor.Red,
        @SerializedName("name") var name: String = "",
        @SerializedName("addresses") var addresses: List<String> = listOf()) : Comparable<Tag> {
    override fun compareTo(other: Tag): Int {
        return this.name.compareTo(other.name)
    }

    fun spannableName(context: Context):Spannable {
        val color = ContextCompat.getColor(context, color.getAndroidColorId())

        val spannableContent = SpannableString(name)
        spannableContent.setSpan(ForegroundColorSpan(color), 0, name.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return spannableContent
    }

    companion object {
        fun new(): Tag {
            val random = (1..100000).random()
            return Tag(random.toString(), color = TagColor.values().random())
        }
    }

}

fun List<Tag>?.createSpannableString(context: Context): Spannable {
    return if (this == null || isEmpty()) SpannableString("") else {
        val spannableBuilder = SpannableStringBuilder()
        forEach {
            val color = ContextCompat.getColor(context, it.color.getAndroidColorId())
            val text = if (indexOf(it) != size - 1) "${it.name}, " else it.name
            spannableBuilder.append(text, ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        spannableBuilder
    }
}

enum class TagColor {
    Red, Orange, Yellow, Green, Blue, Pink;

    fun getAndroidColorName() = when (this) {
        Red -> "Red"
        Orange -> "Orange"
        Yellow -> "Yellow"
        Green -> "Green"
        Blue -> "Blue"
        Pink -> "Pink"
    }

    fun getAndroidColorId() = when (this) {
        Red -> R.color.category_red
        Orange -> R.color.category_orange
        Yellow -> R.color.category_yellow
        Green -> R.color.category_green
        Blue -> R.color.category_blue
        Pink -> R.color.category_pink
    }
}