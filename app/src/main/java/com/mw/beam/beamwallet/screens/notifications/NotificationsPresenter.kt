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

import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.NotificationItem
import com.mw.beam.beamwallet.core.entities.NotificationType
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.runOnUiThread

/**
 *  2/28/19.
 */
class NotificationsPresenter(currentView: NotifcationsContract.View, currentRepository: NotifcationsContract.Repository, private val state: NotificationsState)
    : BasePresenter<NotifcationsContract.View, NotifcationsContract.Repository>(currentView, currentRepository),
        NotifcationsContract.Presenter {

    private lateinit var notificationsSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.privacyMode = repository.isPrivacyModeEnabled()

        view?.init()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        view?.configNotifications(repository.getNotifications(), state.privacyMode)

        notificationsSubscription = AppManager.instance.subOnNotificationsChanged.subscribe {
            App.self.runOnUiThread {
                view?.configNotifications(repository.getNotifications(), state.privacyMode)
            }
        }
    }

    override fun deleteAllNotifications() {
        AppManager.instance.deleteAllNotifications()
    }

    override fun deleteNotifications(list: List<String>) {
        AppManager.instance.deleteAllNotifications(list)
    }

    override fun hasBackArrow(): Boolean? = false
    override fun hasStatus(): Boolean = true

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        view?.createOptionsMenu(menu, inflater,  state.privacyMode)
    }

    override fun onChangePrivacyModePressed() {
        if (!state.privacyMode && repository.isNeedConfirmEnablePrivacyMode()) {
            view?.showActivatePrivacyModeDialog()
        } else {
            state.privacyMode = !state.privacyMode
            repository.setPrivacyModeEnabled(state.privacyMode)
            notifyPrivacyStateChange()
        }
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        state.privacyMode = true
        repository.setPrivacyModeEnabled(state.privacyMode)
        notifyPrivacyStateChange()
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(notificationsSubscription)

    override fun onOpenNotification(notification: NotificationItem) {
        when (notification.type) {
            NotificationType.Address -> {
                view?.openAddressFragment(notification.pId)
            }
            NotificationType.Transaction -> {
                view?.openTransactionFragment(notification.pId)
            }
            NotificationType.Version -> {
                view?.openNewVersionFragment(notification.pId)
            }
        }

        if(!notification.isRead) {
            AppManager.instance.readNotification(notification.nId)
        }
    }

    private fun notifyPrivacyStateChange() {
        view?.configPrivacyStatus(state.privacyMode)
    }
}
