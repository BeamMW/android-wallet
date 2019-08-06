package com.mw.beam.beamwallet.screens.welcome_screen.restore_owner_key

import android.view.View
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import kotlinx.android.synthetic.main.fragment_restore_owner_key.*

class RestoreOwnerKeyFragment: BaseFragment<RestoreOwnerKeyPresenter>(), RestoreOwnerKeyContract.View {

    private val args by lazy {
        RestoreOwnerKeyFragmentArgs.fromBundle(arguments!!)
    }

    override fun getPassword(): String = args.pass

    override fun getSeed(): Array<String> = args.seed

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_restore_owner_key

    override fun getToolbarTitle(): String? = getString(R.string.owner_key)

    override fun init(key: String) {
        progressBar.visibility = View.GONE

        ownerKey.visibility = View.VISIBLE
        ownerKey.text = key
    }

    override fun addListeners() {
        btnCopy.setOnClickListener {
            presenter?.onCopyPressed()
        }

        btnNext.setOnClickListener {
            presenter?.onNextPressed()
        }
    }

    override fun clearListeners() {
        btnCopy.setOnClickListener(null)
        btnNext.setOnClickListener(null)
    }

    override fun showCopiedSnackBar() {
        showSnackBar(getString(R.string.owner_key_copied_message))
    }

    override fun navigateToEnterTrustedNode() {
        findNavController().navigate(RestoreOwnerKeyFragmentDirections.actionRestoreOwnerKeyFragmentToRestoreTustedNodeFragment())
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return RestoreOwnerKeyPresenter(this, RestoreOwnerKeyRepository())
    }

}