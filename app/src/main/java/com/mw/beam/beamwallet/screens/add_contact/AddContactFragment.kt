package com.mw.beam.beamwallet.screens.add_contact

import android.annotation.SuppressLint
import android.content.Intent
import android.text.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.QrHelper
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.createSpannableString
import com.mw.beam.beamwallet.core.views.TagAdapter
import com.mw.beam.beamwallet.screens.qr.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_add_contact.*

class AddContactFragment : BaseFragment<AddContactPresenter>(), AddContactContract.View {
    private val tokenWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.onTokenChanged()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    override fun getAddress(): String {
        return address.text?.toString() ?: ""
    }

    override fun getName(): String {
        return name.text?.toString() ?: ""
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_add_contact

    override fun getToolbarTitle(): String? = getString(R.string.add_contact)

    override fun getStatusBarColor(): Int = ContextCompat.getColor(context!!, R.color.addresses_status_bar_color)

    override fun addListeners() {
        btnCancel.setOnClickListener {
            presenter?.onCancelPressed()
        }

        btnSave.setOnClickListener {
            presenter?.onSavePressed()
        }

        scanQR.setOnClickListener {
            presenter?.onScanPressed()
        }

        tagAction.setOnClickListener {
            presenter?.onTagActionPressed()
        }

        address.addTextChangedListener(tokenWatcher)
        address.filters = arrayOf(InputFilter { source, _, _, dest, dstart, dend ->
            if (source.isNotEmpty()) {
                val enteredText = dest.toString().substring(0 until dstart) + source + dest.substring(dend until dest.length)

                if (!QrHelper.isValidAddress(enteredText)) {
                    ""
                } else {
                    null
                }
            } else {
                null
            }

        })
    }

    override fun setAddress(address: String) {
        this.address.setText(address)
    }

    override fun navigateToScanQr() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.captureActivity = ScanQrActivity::class.java
        integrator.setBeepEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onScannedQR(IntentIntegrator.parseActivityResult(resultCode, data).contents)
    }

    override fun navigateToAddNewCategory() {
        findNavController().navigate(AddContactFragmentDirections.actionAddContactFragmentToEditCategoryFragment())
    }

    override fun setupTagAction(isEmptyTags: Boolean) {
        val resId = if (isEmptyTags) R.drawable.ic_add_tag else R.drawable.ic_edit_tag
        val drawable = ContextCompat.getDrawable(context!!, resId)
        tagAction.setImageDrawable(drawable)
    }

    override fun showCreateTagDialog() {
        showAlert(
                getString(R.string.dialog_empty_tags_message),
                getString(R.string.create_tag),
                { presenter?.onCreateNewTagPressed() },
                getString(R.string.tag_list_is_empty),
                getString(R.string.cancel)
        )
    }

    @SuppressLint("InflateParams")
    override fun showTagsDialog(selectedTags: List<Tag>) {
        BottomSheetDialog(context!!, R.style.common_bottom_sheet_style).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.tags_bottom_sheet, null)
            setContentView(view)

            val tagAdapter = TagAdapter { presenter?.onSelectTags(it) }

            val tagList = view.findViewById<RecyclerView>(R.id.tagList)
            val btnBottomSheetClose = view.findViewById<ImageView>(R.id.btnBottomSheetClose)

            tagList.layoutManager = LinearLayoutManager(context)
            tagList.adapter = tagAdapter

            tagAdapter.setSelectedTags(selectedTags)

            btnBottomSheetClose.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    override fun setTags(tags: List<Tag>) {
        if (tags.count() > 0) {
            this.tags.text = tags.createSpannableString(context!!)
        }
        else{
            this.tags.text = getString(R.string.none)
        }
    }

    override fun showTokenError() {
        tokenError.visibility = View.VISIBLE
    }

    override fun showErrorNotBeamAddress() {
        showSnackBar(getString(R.string.send_error_not_beam_address))
    }

    override fun hideTokenError() {
        tokenError.visibility = View.INVISIBLE
    }

    override fun getAddressFromArguments(): String? {
        if (arguments!=null)
        {
            return AddContactFragmentArgs.fromBundle(arguments!!).address
        }

        return null
    }

    override fun clearListeners() {
        btnCancel.setOnClickListener(null)
        btnSave.setOnClickListener(null)
        scanQR.setOnClickListener(null)
        tagAction.setOnClickListener(null)
        address.removeTextChangedListener(tokenWatcher)
    }

    override fun close() {
        findNavController().popBackStack()
    }

    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return AddContactPresenter(this, AddContactRepository(), AddContactState())
    }
}