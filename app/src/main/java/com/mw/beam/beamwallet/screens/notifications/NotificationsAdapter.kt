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

package com.mw.beam.beamwallet.screens.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.NotificationItem
import com.mw.beam.beamwallet.core.helpers.ScreenHelper
import com.mw.beam.beamwallet.core.helpers.selector
import com.mw.beam.beamwallet.core.utils.CalendarUtils

import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_notification.*


class NotificationsAdapter(private val context: Context,
                           private val clickListener: OnItemClickListener,
                           private val longListener: OnLongClickListener, var data: List<NotificationItem>) : androidx.recyclerview.widget.RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private val NOTIFICATION = 1
    private val SECTION = 2

    private var privacyMode = false

    private var selectedNotifiations = mutableListOf<String>()
    var mode = NotificationsFragment.Mode.NONE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View
        return if (viewType == SECTION) {
            view = LayoutInflater.from(context).inflate(R.layout.item_notification_title, parent, false)
            ViewHolder(view)
        } else {
            notificationHolder(parent, viewType)
        }
    }

    private fun notificationHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)).apply {
        this.containerView.setOnClickListener {
            clickListener.onItemClick(data[adapterPosition])

            if (mode == NotificationsFragment.Mode.EDIT) {
                if (selectedNotifiations.contains(data[adapterPosition].nId)) {
                    selectedNotifiations.remove(data[adapterPosition].nId)
                } else {
                    selectedNotifiations.add(data[adapterPosition].nId)
                }
                checkBox.isChecked = selectedNotifiations.contains(data[adapterPosition].nId)
            }
        }

        if (longListener != null) {
            this.containerView.setOnLongClickListener {
                longListener?.onLongClick(data[adapterPosition])
                return@setOnLongClickListener true
            }
        }
    }

    fun changeSelectedItems(data: List<String>, isAdded: Boolean, item: String?) {
        selectedNotifiations = data.toMutableList()

        if (item != null) {
            for (i in 0 until itemCount) {
                if (item(i).nId == item) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun reloadData(mode: NotificationsFragment.Mode) {
        this.mode = mode
        notifyDataSetChanged()
    }

        @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(getItemViewType(position) == NOTIFICATION) {
            val notificationItem = data[position]

            holder.apply {
                if(!notificationItem.isRead) {
                    val params = mainLayout.layoutParams as RecyclerView.LayoutParams
                    params.bottomMargin = ScreenHelper.dpToPx(context, 5)
                    params.topMargin = ScreenHelper.dpToPx(context, 5)
                    mainLayout.layoutParams = params
                    itemView.selector(R.color.white_01)
                }
                else {
                    if (App.isDarkMode) {
                        itemView.selector(if (position % 2 != 0) R.color.wallet_adapter_multiply_color_dark else R.color.colorClear)
                    }
                    else {
                        itemView.selector(if (position % 2 != 0) R.color.wallet_adapter_multiply_color else R.color.colorClear)
                    }
                }

                if(notificationItem.icon != null) {
                    icon.setImageResource(notificationItem.icon!!)
                }
                name.text = notificationItem.name

                if(notificationItem.detailSpannable != null) {
                    detail.text = notificationItem.detailSpannable
                    detail.visibility = View.VISIBLE
                }
                else {
                    detail.text = notificationItem.detail
                    detail.visibility = if (notificationItem.detail == null) View.GONE else View.VISIBLE
                }

                categories.visibility = View.GONE

                date.text = CalendarUtils.fromTimestamp(notificationItem.date)

                if (mode == NotificationsFragment.Mode.NONE) {
                    checkBox.isChecked = false
                    checkBox.visibility = View.GONE
                } else {
                    checkBox.isChecked = selectedNotifiations.contains(notificationItem.nId)
                    checkBox.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int {
        val notificationItem = data[position]
        return if (notificationItem.name == context.getString(R.string.read)) {
            SECTION
        }
        else {
            NOTIFICATION
        }
    }

    fun item(index: Int): NotificationItem {
        return data[index]
    }

    fun setPrivacyMode(isEnable: Boolean) {
        privacyMode = isEnable
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: NotificationItem)
    }

    interface OnLongClickListener {
        fun onLongClick(item: NotificationItem)
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}
