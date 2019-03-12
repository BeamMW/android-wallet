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

package com.mw.beam.beamwallet.core.watchers

import android.text.InputFilter
import android.text.Spanned

/**
 * Created by vain onnellinen on 2/11/19.
 */
class AmountFilter : InputFilter {
    private val regExp = "^(([1-9][0-9]{0,7})|(1[0-9]{8})|(2[0-4][0-9]{7})|(25[0-3][0-9]{6})|(0))(\\.[0-9]{0,7}[1-9]?)?$".toRegex()

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        if (source.isNotEmpty()) {
            return if (!regExp.containsMatchIn(dest.toString().substring(0 until dstart) + source + dest.substring(dend until dest.length))) {
                ""
            } else {
                null
            }
        }

        return null
    }
}
