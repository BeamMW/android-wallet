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
import kotlinx.android.synthetic.main.fragment_transactions.*
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
import kotlinx.android.synthetic.main.toolbar.*


class TransactionsFragment : BaseFragment<TransactionsPresenter>(), TransactionsContract.View {
    private lateinit var pageAdapter: TransactionsPageAdapter

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_transactions
    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun init() {
        pageAdapter = TransactionsPageAdapter(context!!) { presenter?.onTransactionPressed(it) }
        pageAdapter.setPrivacyMode(presenter?.repository?.isPrivacyModeEnabled() == true)

        pager.adapter = pageAdapter
        tabLayout.setupWithViewPager(pager)
    }

    override fun configTransactions(transactions: List<TxDescription>) {
        pageAdapter.setData(transactions)
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
        inflater.inflate(R.menu.wallet_transactions_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter?.onSearchPressed()
            R.id.menu_proof -> presenter?.onProofVerificationPressed()
            R.id.menu_export ->  {
                registerForContextMenu(toolbar)
                toolbar.showContextMenu()
                unregisterForContextMenu(toolbar)
            }
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
}