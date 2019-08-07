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

package com.mw.beam.beamwallet.screens.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.LocaleHelper

class LanguageAdapter(private val languages: List<LocaleHelper.SupportedLanguage>, private val onSelected: (LocaleHelper.SupportedLanguage) -> Unit): RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private var selectedLanguage: LocaleHelper.SupportedLanguage? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun getItemCount(): Int = languages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languages[position]

        holder.selected = selectedLanguage?.languageCode == language.languageCode
        holder.englishName = language.englishName
        holder.nativeName = language.nativeName
        holder.itemView.setOnClickListener { onSelected(language) }
    }

    fun setSelected(language: LocaleHelper.SupportedLanguage) {
        selectedLanguage = language
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var selected: Boolean = false
            set(value) {
                field = value
                val visibility = if (field) View.VISIBLE else View.GONE
                itemView.findViewById<ImageView>(R.id.selectedImage).visibility = visibility
            }

        var englishName: String = ""
            set(value) {
                field = value
                itemView.findViewById<TextView>(R.id.language).text = field
            }

        var nativeName: String = ""
            set(value) {
                field = value
                itemView.findViewById<TextView>(R.id.nativeLanguageName).text = field
            }
    }
}