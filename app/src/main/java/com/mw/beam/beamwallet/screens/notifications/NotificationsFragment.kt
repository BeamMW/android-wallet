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

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.*
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.entities.NotificationItem
import com.mw.beam.beamwallet.core.views.NotificationBanner

import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.fragment_notifications.*


class NotificationsFragment : BaseFragment<NotificationsPresenter>(), NotifcationsContract.View {
    enum class Mode {
        NONE, EDIT
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_notifications
    override fun getToolbarTitle(): String? = getString(R.string.notifications)

    private var selectedNotifications = mutableListOf<String>()

    private var mode = Mode.NONE
    private var menuPosition = 0

    private lateinit var adapter: NotificationsAdapter

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mode == Mode.NONE) {
                showWalletFragment()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)

        itemsswipetorefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.WHITE)
        itemsswipetorefresh.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

        itemsswipetorefresh.setOnRefreshListener {
            AppManager.instance.reload()
            android.os.Handler().postDelayed({
                if (itemsswipetorefresh!=null) {
                    itemsswipetorefresh.isRefreshing = false
                }
            }, 1000)
        }
    }

    override fun init() {
        setHasOptionsMenu(true)
        setMenuVisibility(true)

        initNotificationsList()

        (activity as? AppActivity)?.enableLeftMenu(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as? AppActivity)?.openMenu()
        }
    }

    private fun initNotificationsList() {
        val context = context ?: return

        adapter = NotificationsAdapter(context, object: NotificationsAdapter.OnItemClickListener{
            override fun onItemClick(item: NotificationItem) {
                presenter?.onOpenNotification(item)
            }
        }, object: NotificationsAdapter.OnLongClickListener {
            override fun onLongClick(item: NotificationItem) {
            }
        }, listOf())

        notificationsListView.layoutManager = LinearLayoutManager(context)
        notificationsListView.adapter = adapter
    }

    override fun addListeners() {
        super.addListeners()

        btnClearAll.setOnClickListener {

        }
    }

    override fun clearListeners() {
        super.clearListeners()
        btnClearAll.setOnClickListener(null)
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback.isEnabled = true
    }

    override fun onStop() {
        onBackPressedCallback.isEnabled = false
        super.onStop()
    }

    override fun onDestroy() {
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        super.onDestroy()
    }

    override fun getStatusBarColor(): Int = if (App.isDarkMode) {
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color_black)  }  else{
    ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)  }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return  NotificationsPresenter(this, NotificationsRepository(), NotificationsState())
    }

    override fun configNotifications(notifications: List<NotificationItem>, isEnablePrivacyMode: Boolean) {
        if(notificationsListView != null) {
            notificationsListView.visibility = if (notifications.isEmpty()) View.GONE else View.VISIBLE
            btnClearAll.visibility = if (notifications.isEmpty()) View.GONE else View.VISIBLE
            emptyLayout.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE

            if (notifications.isNotEmpty()) {
                adapter.setPrivacyMode(isEnablePrivacyMode)
                adapter.data = notifications
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter?.onCreateOptionsMenu(menu, inflater)
    }

    override fun createOptionsMenu(menu: Menu?, inflater: MenuInflater?, isEnablePrivacyMode: Boolean) {
        inflater?.inflate(R.menu.privacy_menu, menu)
        val menuItem = menu?.findItem(R.id.privacy_mode)
        menuItem?.setOnMenuItemClickListener {
            presenter?.onChangePrivacyModePressed()
            false
        }

        menuItem?.setIcon(if (isEnablePrivacyMode) R.drawable.ic_eye_crossed else R.drawable.ic_icon_details)
    }

    override fun showActivatePrivacyModeDialog() {
        showAlert(getString(R.string.common_security_mode_message), getString(R.string.activate), { presenter?.onPrivacyModeActivated() }, getString(R.string.common_security_mode_title), getString(R.string.cancel), { presenter?.onCancelDialog() })
    }

    override fun configPrivacyStatus(isEnable: Boolean) {
        activity?.invalidateOptionsMenu()
        adapter.setPrivacyMode(isEnable)
    }

    override fun openAddressFragment(id: String) {
        val address = AppManager.instance.getAddress(id)
        if (address!=null) {
            findNavController().navigate(NotificationsFragmentDirections.actionNotificationsFragmentToAddressFragment(address))
        }
    }

    override fun openTransactionFragment(id: String) {
        findNavController().navigate(NotificationsFragmentDirections.actionNotificationsFragmentToTransactionDetailsFragment(id))
    }

    override fun openNewVersionFragment(value: String) {
        findNavController().navigate(NotificationsFragmentDirections.actionNotificationsFragmentToNewVersionFragment(value))
    }
}
