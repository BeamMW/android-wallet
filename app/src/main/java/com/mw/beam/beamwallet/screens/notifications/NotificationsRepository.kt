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

import android.content.Context
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.NotificationItem
import com.mw.beam.beamwallet.core.helpers.PreferencesManager


class NotificationsRepository : BaseRepository(), NotifcationsContract.Repository {

    override fun getNotifications(context: Context): List<NotificationItem> {
        val notifications =  AppManager.instance.getNotifications()
        val unread = mutableListOf<NotificationItem>()
        val read = mutableListOf<NotificationItem>()
        val result = mutableListOf<NotificationItem>()

        notifications.forEach {
            if (it.isRead) {
                read.add(NotificationItem(it, context))
            }
            else {
                unread.add(NotificationItem(it, context))
            }
        }
        read.sortByDescending { it.date  }
        unread.sortByDescending { it.date  }

        result.addAll(unread)
        if(read.count() > 0) {
            read.add(0, NotificationItem(context.getString(R.string.read)))
        }
        result.addAll(read)

        return result
    }

    override fun isNeedConfirmEnablePrivacyMode(): Boolean = PreferencesManager.getBoolean(PreferencesManager.KEY_PRIVACY_MODE_NEED_CONFIRM, true)
}
