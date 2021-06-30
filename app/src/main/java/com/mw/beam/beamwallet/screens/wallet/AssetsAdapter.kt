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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_asset.*

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.entities.Asset
import com.mw.beam.beamwallet.core.helpers.ScreenHelper
import com.mw.beam.beamwallet.core.helpers.convertToAssetString
import com.mw.beam.beamwallet.core.helpers.exchangeValueAsset


class AssetsAdapter(private val context: Context, private var data: List<Asset>, private val clickListener: (Asset) -> Unit) :
        RecyclerView.Adapter<AssetsAdapter.ViewHolder>() {

    fun reloadData(list: List<Asset>) {
        data = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long {
        val data = data[position]
        return data.assetId.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_asset, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.invoke(data[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val asset = data[position]

        holder.apply {
            assetLabel.text = asset.available.convertToAssetString(asset.unitName)
            assetSecondLabel.text = asset.available.exchangeValueAsset(asset.assetId)
            assetIcon.setImageResource(asset.image)
            gradientView.start = Color.parseColor(asset.color)

            if (asset.lockedSum() == 0L) {
                val layoutParams = gradientView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.height = ScreenHelper.dpToPx(context, 60)
                gradientView.layoutParams = layoutParams

                val balanceParams = balanceLayout.layoutParams as LinearLayout.LayoutParams
                balanceParams.topMargin = ScreenHelper.dpToPx(context, 2)
                balanceLayout.layoutParams = balanceParams

                lockLabel.visibility = View.GONE
            }
            else {
                val layoutParams = gradientView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.height = ScreenHelper.dpToPx(context, 80)
                gradientView.layoutParams = layoutParams

                val balanceParams = balanceLayout.layoutParams as LinearLayout.LayoutParams
                balanceParams.topMargin = ScreenHelper.dpToPx(context, 0)
                balanceLayout.layoutParams = balanceParams

                lockLabel.visibility = View.VISIBLE
                lockLabel.text = asset.lockedSum().convertToAssetString(asset.unitName)
            }
        }
    }


    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}