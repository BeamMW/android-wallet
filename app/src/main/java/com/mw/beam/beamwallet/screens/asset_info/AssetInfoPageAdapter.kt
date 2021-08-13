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


package com.mw.beam.beamwallet.screens.asset_info

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class AssetInfoPageAdapter(val assetId: Int, fm: FragmentManager, private val totalTabs: Int) : FragmentStatePagerAdapter(fm) {
    var onClick: (() -> Unit)? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = AssetBalanceFragment.newInstance(assetId)
                fragment.onClick = {
                    onClick?.invoke()
                }
                return fragment
            }
            else -> AssetDescriptionFragment.newInstance(assetId)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}