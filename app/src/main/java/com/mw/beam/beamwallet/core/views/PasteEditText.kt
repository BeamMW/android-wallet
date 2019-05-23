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
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.mw.beam.beamwallet.core.watchers.TextWatcher


/**
 * Created by vain onnellinen on 3/25/19.
 */
class PasteEditText : AppCompatEditText {
    private val listeners: ArrayList<TextWatcher> = arrayListOf()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun addListener(listener: TextWatcher) {
        try {
            super.addTextChangedListener(listener)
            listeners.add(listener)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun removeListener(listener: TextWatcher) {
        listeners.remove(listener)
        super.removeTextChangedListener(listener)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            onTextPaste()
        }

        return super.onTextContextMenuItem(id)
    }

    private fun onTextPaste() {
        listeners.filter { it is PasteEditTextWatcher }
                .forEach { (it as PasteEditTextWatcher).onPaste() }
    }
}

interface PasteEditTextWatcher : TextWatcher {
    fun onPaste()
}
