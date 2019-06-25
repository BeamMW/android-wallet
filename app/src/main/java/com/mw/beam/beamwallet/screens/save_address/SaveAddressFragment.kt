package com.mw.beam.beamwallet.screens.save_address

import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import kotlinx.android.synthetic.main.fragment_save_address.*

class SaveAddressFragment: BaseFragment<SaveAddressPresenter>(), SaveAddressContract.View {
    override fun getAddress(): String = SaveAddressFragmentArgs.fromBundle(arguments!!).address

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_save_address

    override fun init(address: String) {
        this.address.text = address
    }

    override fun addListeners() {
        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        btnCancel.setOnClickListener {
            presenter?.onCancelPressed()
        }
    }

    override fun clearListeners() {
        btnSave.setOnClickListener(null)
        btnCancel.setOnClickListener(null)
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SaveAddressPresenter(this, SaveAddressRepository())
    }

    override fun getToolbarTitle(): String? = getString(R.string.save_address)
}