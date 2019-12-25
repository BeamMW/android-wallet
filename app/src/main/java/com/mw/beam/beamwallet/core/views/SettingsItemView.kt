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

package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.content.res.ColorStateList
import android.text.Spannable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App

import com.mw.beam.beamwallet.screens.settings.SettingsFragment

import kotlinx.android.synthetic.main.item_settings.view.*

class SettingsItemView: FrameLayout {
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    var iconResId: Int? = null
        set(value) {
            field = value

            if (value != null) {
                iconView.visibility = View.VISIBLE
                iconView.setImageDrawable(resources.getDrawable(value,App.self.theme))
                val colorRes  = if (App.isDarkMode) {
                    R.color.common_text_dark_color_dark
                }
                else{
                    R.color.common_text_dark_color
                }
                iconView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, colorRes))
            } else {
                iconView.visibility = View.GONE
                labelsLayout.setPadding(15,0,15,0)
                this.setPadding(0,0,0,0)
            }
        }

    var text: String = ""
        set(value) {
            field = value

            if(value.isNullOrEmpty()) {
                textLabel.visibility = View.GONE
            }
            else{
                textLabel.text = field
            }
        }

    var detail: String? = ""
    set(value) {
        field = value
        detailLabel.text = field

        if(value.isNullOrEmpty()) {
            detailLabel.visibility = View.GONE
        }
        else{
            detailLabel.visibility = View.VISIBLE
        }
    }

    var spannable: Spannable? = null
        set(value) {
            field = value
            if(value!=null) {
                detailLabel.text = field
                detailLabel.visibility = View.VISIBLE
            }
        }

    var switch: Boolean? = null
        set(value) {
            field = value

            if(value == null) {
                switchView.visibility = View.GONE
            }
            else{
                cardItem.setPadding(0,12,0,0)
                cardItem.foreground = null
                switchView.isChecked = value
                switchView.visibility = View.VISIBLE
            }
        }

    var mode:SettingsFragment.Mode = SettingsFragment.Mode.RemoveWallet

    override fun setOnClickListener(l: OnClickListener?) {
        if (switch==null) {
            cardItem.setOnClickListener(l)
        }
        else{
            switchView.setOnClickListener(l)
        }
    }

    private fun init(context: Context) {
        inflate(context, R.layout.item_settings, this)
    }
}