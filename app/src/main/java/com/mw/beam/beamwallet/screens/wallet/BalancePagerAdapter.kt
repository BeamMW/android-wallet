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

package com.mw.beam.beamwallet.screens.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.helpers.convertToCurrencyString

class BalancePagerAdapter(val context: Context): androidx.viewpager.widget.PagerAdapter() {

    var available: Long = 0
    var maturing: Long = 0

    //tablayout
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.wallet_balanse_item, container, false)

        view.findViewById<TextView>(R.id.balance).text = when (BalanceTab.values()[position]) {
            BalanceTab.Available -> available
            BalanceTab.Maturing -> maturing
        }.convertToBeamString()

        view.findViewById<TextView>(R.id.secondBalance).text = when (BalanceTab.values()[position]) {
            BalanceTab.Available -> available
            BalanceTab.Maturing -> maturing
        }.convertToCurrencyString()

        container.addView(view)
        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        return  POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun getCount(): Int {
        return if (maturing > 0) BalanceTab.values().size else 1
    }

}

enum class BalanceTab {
    Available, Maturing
}