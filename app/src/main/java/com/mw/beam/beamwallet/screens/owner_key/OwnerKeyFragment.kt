package com.mw.beam.beamwallet.screens.owner_key

import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import kotlinx.android.synthetic.main.fragment_owner_key.*

class OwnerKeyFragment: BaseFragment<OwnerKeyPresenter>(), OwnerKeyContract.View {

    override fun getToolbarTitle(): String? = getString(R.string.owner_key_toolbar_title)
    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_owner_key

    override fun init(key: String) {
        progressBar.visibility = View.GONE
        ownerKeyValue.visibility = View.VISIBLE

        ownerKeyValue.text = key
    }

    override fun addListeners() {
        btnCopy.setOnClickListener {
            presenter?.onCopyPressed()
        }
    }

    override fun clearListeners() {
        btnCopy.setOnClickListener(null)
    }

    override fun showCopiedSnackBar() {
        showSnackBar(getString(R.string.owner_key_copied_message))
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return OwnerKeyPresenter(this, OwnerKeyRepository(), OwnerKeyState())
    }

}