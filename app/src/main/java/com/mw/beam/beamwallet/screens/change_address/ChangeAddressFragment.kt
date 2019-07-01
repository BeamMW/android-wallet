package com.mw.beam.beamwallet.screens.change_address

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import com.mw.beam.beamwallet.core.helpers.PermissionsHelper
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_change_address.*

class ChangeAddressFragment : BaseFragment<ChangeAddressPresenter>(), ChangeAddressContract.View {

    companion object {
        var callback: ChangeAddressCallback? = null
    }

    private lateinit var adapter: SearchAddressesAdapter

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onChangeSearchText(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    override fun isFromReceive(): Boolean = ChangeAddressFragmentArgs.fromBundle(arguments!!).isFromReceive

    override fun getToolbarTitle(): String? = getString(R.string.change_address)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_change_address

    override fun init(state: ChangeAddressContract.ViewState) {
        gradientView.setBackgroundResource(if (state == ChangeAddressContract.ViewState.Receive) R.drawable.receive_toolbar_gradient else R.drawable.send_toolbar_gradient)

        adapter = SearchAddressesAdapter(context!!, object : SearchAddressesAdapter.OnSearchAddressClickListener {
            override fun onClick(walletAddress: WalletAddress) {
                presenter?.onItemPressed(walletAddress)
            }
        })

        addressesRecyclerView.layoutManager = LinearLayoutManager(context)
        addressesRecyclerView.adapter = adapter
    }

    override fun addListeners() {
        searchAddress.addTextChangedListener(textWatcher)
        scanQR.setOnClickListener {
            presenter?.onScanQrPressed()
        }
    }

    override fun clearListeners() {
        searchAddress.removeTextChangedListener(textWatcher)
        scanQR.setOnClickListener(null)
    }

    override fun updateList(items: List<SearchItem>) {
        adapter.setData(items)
    }

    override fun back(walletAddress: WalletAddress) {
        callback?.onChangeAddress(walletAddress)
        findNavController().popBackStack()
    }

    override fun isPermissionGranted(): Boolean {
        return PermissionsHelper.requestPermissions(this, PermissionsHelper.PERMISSIONS_CAMERA, PermissionsHelper.REQUEST_CODE_PERMISSION)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var isGranted = true

        for ((index, permission) in permissions.withIndex()) {
            if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                isGranted = false
                if (!shouldShowRequestPermissionRationale(permission)) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.NEVER_ASK_AGAIN)
                } else if (PermissionsHelper.PERMISSIONS_CAMERA == permission) {
                    presenter?.onRequestPermissionsResult(PermissionStatus.DECLINED)
                }
            }
        }

        if (isGranted) {
            presenter?.onRequestPermissionsResult(PermissionStatus.GRANTED)
        }
    }

    override fun showPermissionRequiredAlert() {
        showAlert(message = getString(R.string.send_permission_required_message),
                btnConfirmText = getString(R.string.settings),
                onConfirm = { showAppDetailsPage() },
                title = getString(R.string.send_permission_required_title),
                btnCancelText = getString(R.string.cancel))
    }

    private fun showAppDetailsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context?.packageName}")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onScannedQR(IntentIntegrator.parseActivityResult(resultCode, data).contents)
    }

    override fun setAddress(address: String) {
        searchAddress.setText(address)
    }

    override fun scanQR() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.captureActivity = ScanQrActivity::class.java
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun showNotBeamAddressError() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ChangeAddressPresenter(this, ChangeAddressRepository(), ChangeAddressState())
    }
}