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

package com.mw.beam.beamwallet.screens.transactions

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.entities.TxDescription
import java.io.File
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment
import java.io.FileOutputStream
import java.io.IOException
import android.view.*
import android.net.Uri
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import androidx.activity.OnBackPressedCallback
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.screens.wallet.TransactionsAdapter
import kotlinx.android.synthetic.main.fragment_transactions.pager
import kotlinx.android.synthetic.main.fragment_transactions.tabLayout
import kotlinx.android.synthetic.main.fragment_transactions.toolbarLayout
import kotlinx.android.synthetic.main.toolbar.*

class TransactionsFragment : BaseFragment<TransactionsPresenter>(), TransactionsContract.View {
    enum class Mode {
        NONE, EDIT
    }

    private var selectedTransactions= mutableListOf<String>()
    private var mode = Mode.NONE

    private var pageAdapter: TransactionsPageAdapter? = null

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mode == Mode.NONE) {
                findNavController().popBackStack()
            } else {
                cancelSelectedTransactions()
            }
        }
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

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_transactions
    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(activity!!, onBackPressedCallback)
        toolbarLayout.hasStatus = true
    }

    override fun init() {
        if (pageAdapter!=null)
        {
            if (pager.adapter == null) {
                pager.adapter = pageAdapter
                tabLayout.setupWithViewPager(pager)
            }
        }
        else{
            pageAdapter = TransactionsPageAdapter(context!!,
                    object : TransactionsAdapter.OnLongClickListener {
                        override fun onLongClick(item: TxDescription) {
                            if (mode == Mode.NONE) {
                                presenter?.onModeChanged(Mode.EDIT)

                                selectedTransactions.add(item.id)

                                pageAdapter?.changeSelectedItems(selectedTransactions, true, item.id)

                                pageAdapter?.reloadData(mode)

                                onSelectedTransactionsChanged()
                            }
                        }
                    }
            )
            {

                if (mode == Mode.NONE) {
                    presenter?.onTransactionPressed(it)
                }
                else{
                    if (selectedTransactions.contains(it.id)) {
                        selectedTransactions.remove(it.id)
                    } else {
                        selectedTransactions.add(it.id)
                    }

                    onSelectedTransactionsChanged()
                }
            }
            pageAdapter?.setPrivacyMode(presenter?.repository?.isPrivacyModeEnabled() == true)

            pager.adapter = pageAdapter
            tabLayout.setupWithViewPager(pager)
        }
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        pageAdapter?.setData(transactions)
    }

    override fun showProofVerification() {
        findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToProofVerificationFragment())
    }

    override fun showSearchTransaction() {
        findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToSearchTransactionFragment())
    }

    override fun showTransactionDetails(txId: String) {
        findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToTransactionDetailsFragment(txId))
    }

    override fun showRepeatTransaction() {
        val transaction = AppManager.instance.getTransaction(selectedTransactions.first())
        if (transaction!=null) {
            mode = Mode.NONE
            selectedTransactions.clear()

            if (transaction.sender.value) {
                findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToSendFragment(transaction.peerId, transaction.amount))
            } else {
                val address = AppManager.instance.getAddress(transaction.myId)
                findNavController().navigate(TransactionsFragmentDirections.actionTransactionsFragmentToReceiveFragment(transaction.amount, address))
            }
        }
    }

    override fun exportSave(content: String) {
        val fileName = "transactions_" + System.currentTimeMillis() + ".csv"

        Dexter.withActivity(activity)
                .withPermission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : PermissionListener {

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val outputStream: FileOutputStream
                        try {
                            val file2 = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), fileName)
                            outputStream = FileOutputStream(file2)
                            outputStream.write(content.toByteArray())
                            outputStream.close()

                            showSnackBar(getString(R.string.saved_to_downloads))

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        if (response?.isPermanentlyDenied == true){
                            showAlert(message = getString(R.string.storage_permission_required_message_small),
                                    btnConfirmText = getString(R.string.settings),
                                    onConfirm = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = Uri.fromParts("package", context?.packageName, null)
                                        startActivity(intent)
                                    },
                                    title = getString(R.string.send_permission_required_title),
                                    btnCancelText = getString(R.string.cancel))
                        }
                    }

                }).check()
    }

    override fun exportShare(file: File) {
        val context = context ?: return

        val uri = FileProvider.getUriForFile(context, AppConfig.AUTHORITY, file)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.common_share_title)))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (mode == Mode.EDIT) {
            inflater.inflate(R.menu.wallet_transactions_menu_2, menu)
            menu.findItem(R.id.repeat).isVisible = selectedTransactions.count() == 1
        }
        else{
            inflater.inflate(R.menu.wallet_transactions_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter?.onSearchPressed()
            R.id.menu_proof -> presenter?.onProofVerificationPressed()
            R.id.repeat -> presenter?.onRepeatTransaction()
            R.id.menu_export ->  {

                val view = LayoutInflater.from(context).inflate(R.layout.dialog_share, null)

                view.findViewById<TextView>(R.id.cancelBtn).setOnClickListener {
                    dialog?.dismiss()
                }

                view.findViewById<TextView>(R.id.shareBtn).setOnClickListener {
                    dialog?.dismiss()
                    presenter?.onExportShare()
                }

                view.findViewById<TextView>(R.id.saveBtn).setOnClickListener {
                    dialog?.dismiss()
                    presenter?.onExportSave()
                }

                dialog = AlertDialog.Builder(context!!).setView(view).show().apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }
            R.id.delete -> presenter?.onDeleteTransactionsPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return TransactionsPresenter(this, TransactionsRepository())
    }

    override fun getToolbarTitle(): String? = getString(R.string.transactions)

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val save = SpannableStringBuilder()
        save.append(getString(R.string.save_to_downloads))
        save.setSpan(ForegroundColorSpan(Color.WHITE),
                0, save.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val share = SpannableStringBuilder()
        share.append(getString(R.string.share))
        share.setSpan(ForegroundColorSpan(Color.WHITE),
                0, share.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        menu.add(0, 1, 0, save)
        menu.add(0, 2, 0, share)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            presenter?.onExportSave()
        }
        else if (item.itemId == 2) {
            presenter?.onExportShare()
        }

        return super.onContextItemSelected(item)
    }

    private fun onSelectedTransactionsChanged() {
        val toolbarLayout = toolbarLayout
        toolbarLayout.centerTitle = false
        toolbarLayout.toolbar.title = selectedTransactions.count().toString() + " " + getString(R.string.selected).toLowerCase()
        toolbarLayout.toolbar.setNavigationIcon(R.drawable.ic_btn_cancel)
        toolbarLayout.toolbar.setNavigationOnClickListener {
            if (mode == Mode.NONE) {
                findNavController().popBackStack()
            } else {
                cancelSelectedTransactions()
            }
        }

        if (selectedTransactions.count() == 0) {
            cancelSelectedTransactions()
        } else {
            activity?.invalidateOptionsMenu()
        }
    }

    private fun cancelSelectedTransactions() {
        val toolbarLayout = toolbarLayout
        toolbarLayout.centerTitle = false
        toolbarLayout.toolbar.title = getString(R.string.transactions)
        toolbarLayout.toolbar.setNavigationIcon(R.drawable.ic_back)

        presenter?.onModeChanged(Mode.NONE)

        selectedTransactions.clear()

        pageAdapter?.changeSelectedItems(selectedTransactions, false, null)

        pageAdapter?.reloadData(mode)

        activity?.invalidateOptionsMenu()
    }

    override fun deleteTransactions() {
        showDeleteTransactionsSnackBar()
    }

    override fun showDeleteTransactionsSnackBar() {
        val snackText = when {
            selectedTransactions.count() > 1 -> getString(R.string.transactions_deleted)
            else -> getString(R.string.transaction_deleted)
        }

        val titleText = when {
            selectedTransactions.count() > 1 -> getString(R.string.delete_transactions)
            else -> getString(R.string.delete_transaction)
        }

        val msgText = when {
            selectedTransactions.count() > 1 -> getString(R.string.delete_transactions_text)
            else -> getString(R.string.delete_transaction_text)
        }

        showAlert(msgText,getString(R.string.delete),{
            showSnackBar(snackText,
                    onDismiss = {
                        presenter?.removedTransactions?.forEach { id ->
                            TrashManager.remove(id)
                        }
                    },
                    onUndo = {
                        presenter?.removedTransactions?.forEach { id ->
                            TrashManager.restore(id)
                        }
                        presenter?.removedTransactions?.clear()
                    }
            )

            presenter?.onConfirmDeleteTransactions(selectedTransactions)

            cancelSelectedTransactions()

        },titleText,getString(R.string.cancel))
    }

    override fun changeMode(mode: Mode) {
        this.mode = mode
        tabLayout.setMode(mode)
        pager.setMode(mode)
    }
}