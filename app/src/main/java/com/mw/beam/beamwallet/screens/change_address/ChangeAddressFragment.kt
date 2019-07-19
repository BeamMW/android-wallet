package com.mw.beam.beamwallet.screens.change_address

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
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

    override fun getGeneratedAddress(): WalletAddress? = ChangeAddressFragmentArgs.fromBundle(arguments!!).generatedAddress

    override fun getToolbarTitle(): String? = getString(R.string.change_address)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_change_address

    override fun getSearchText(): String = searchAddress.text?.toString() ?: ""

    override fun init(state: ChangeAddressContract.ViewState, generatedAddress: WalletAddress?) {
        gradientView.setBackgroundResource(if (state == ChangeAddressContract.ViewState.Receive) R.drawable.receive_toolbar_gradient else R.drawable.send_toolbar_gradient)

        adapter = SearchAddressesAdapter(context!!, generatedAddress, object : SearchAddressesAdapter.OnSearchAddressClickListener {
            override fun onClick(walletAddress: WalletAddress) {
                presenter?.onItemPressed(walletAddress)
            }
        })

        addressesRecyclerView.layoutManager = LinearLayoutManager(context)
        addressesRecyclerView.adapter = adapter
    }

    override fun getStatusBarColor(): Int {
        return ContextCompat.getColor(context!!, if (isFromReceive()) R.color.received_color else R.color.sent_color)
    }

    override fun addListeners() {
        searchAddress.addTextChangedListener(textWatcher)
    }

    override fun clearListeners() {
        searchAddress.removeTextChangedListener(textWatcher)
    }

    override fun updateList(items: List<SearchItem>) {
        adapter.setData(items)
    }

    override fun back(walletAddress: WalletAddress?) {
        if (walletAddress != null) {
            callback?.onChangeAddress(walletAddress)
        }

        findNavController().popBackStack()
    }

    override fun setAddress(address: String) {
        searchAddress.setText(address)
    }

    override fun showNotBeamAddressError() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return ChangeAddressPresenter(this, ChangeAddressRepository(), ChangeAddressState())
    }
}