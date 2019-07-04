package com.mw.beam.beamwallet.screens.send_confirmation

import android.annotation.SuppressLint
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.base_screen.BaseFragment
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.core.watchers.TextWatcher
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import com.mw.beam.beamwallet.screens.app_activity.PendingSendInfo
import kotlinx.android.synthetic.main.fragment_send_confirmation.*

class SendConfirmationFragment : BaseFragment<SendConfirmationPresenter>(), SendConfirmationContract.View {
    private val args: SendConfirmationFragmentArgs by lazy {
        SendConfirmationFragmentArgs.fromBundle(arguments!!)
    }

    private val passWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            presenter?.onPasswordChanged()
        }
    }

    private val foregroundStartColorSpan by lazy { ForegroundColorSpan(resources.getColor(R.color.sent_color, context?.theme)) }
    private val foregroundEndColorSpan by lazy { ForegroundColorSpan(resources.getColor(R.color.sent_color, context?.theme)) }

    override fun getToolbarTitle(): String? = getString(R.string.confirmation)

    override fun onControllerGetContentLayoutId(): Int = R.layout.fragment_send_confirmation

    override fun getAddress(): String = args.sendAddress
    override fun getOutgoingAddress(): String = args.outgoingAddress
    override fun getAmount(): Long = args.sendAmount
    override fun getFee(): Long = args.fee
    override fun getComment(): String? = args.comment

    @SuppressLint("SetTextI18n")
    override fun init(address: String, outgoingAddress: String, amount: Double, fee: Long, isEnablePasswordConfirm: Boolean) {
        changeUtxoTitle.text = "${getString(R.string.change).toUpperCase()} (${getString(R.string.change_description)})"

        sendTo.text = address

        val length = address.length
        val spannable = SpannableStringBuilder.valueOf(address)


        spannable.setSpan(foregroundStartColorSpan, 0, if (length < 7) length else 6, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable.setSpan(foregroundEndColorSpan, if (length - 6 < 0) 0 else length - 6, length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        sendTo.text = spannable

        this.outgoingAddress.text = outgoingAddress
        amountToSend.text = "${amount.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        this.fee.text = "$fee ${getString(R.string.currency_groth).toUpperCase()}"

        passLayout.visibility = if (isEnablePasswordConfirm) View.VISIBLE else View.GONE
        passError.visibility = if (isEnablePasswordConfirm) View.INVISIBLE else View.GONE
    }

    override fun configureContact(walletAddress: WalletAddress, category: Category?) {
        if (!walletAddress.label.isBlank()) {
            contactName.visibility = View.VISIBLE
            contactName.text = walletAddress.label
        }

        if (category != null) {
            contactCategory.visibility = View.VISIBLE
            contactCategory.text = category.name
            contactCategory.setTextColor(resources.getColor(category.color.getAndroidColorId(), context?.theme))
        }
    }

    override fun addListeners() {
        btnSend.setOnClickListener {
            presenter?.onSendPressed()
        }

        pass.addTextChangedListener(passWatcher)
    }

    override fun clearListeners() {
        btnSend.setOnClickListener(null)
        pass.removeTextChangedListener(passWatcher)
    }

    @SuppressLint("SetTextI18n")
    override fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double) {
        totalUtxoTitle.visibility = View.VISIBLE
        totalUtxo.visibility = View.VISIBLE
        changeUtxoTitle.visibility = View.VISIBLE
        changeUtxo.visibility = View.VISIBLE

        totalUtxo.text = "${usedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
        changeUtxo.text = "${changedUtxo.convertToBeamString()} ${getString(R.string.currency_beam).toUpperCase()}"
    }

    override fun delaySend(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long) {
        (activity as? AppActivity)?.pendingSend(PendingSendInfo(token, comment, amount, fee, outgoingAddress))
    }

    override fun getPassword(): String = pass.text?.toString() ?: ""

    override fun clearPasswordError() {
        passError.visibility = View.INVISIBLE
        pass.isStateAccent = true
    }

    override fun showWrongPasswordError() {
        pass.isStateError = true
        passError.text = getString(R.string.pass_wrong)
        passError.visibility = View.VISIBLE
    }

    override fun showEmptyPasswordError() {
        pass.isStateError = true
        passError.text = getString(R.string.password_can_not_be_empty)
        passError.visibility = View.VISIBLE
    }

    override fun showSaveAddressFragment(address: String) {
        findNavController().navigate(SendConfirmationFragmentDirections.actionSendConfirmationFragmentToSaveAddressFragment(address))
    }

    override fun showSaveContactDialog() {
        showAlert(
                getString(R.string.save_recipient_address),
                getString(R.string.save),
                { presenter?.onSaveContactPressed() },
                getString(R.string.save_address),
                getString(R.string.dont_save),
                { presenter?.onCancelSaveContactPressed() }
        )
    }

    override fun showWallet() {
        findNavController().popBackStack(R.id.walletFragment, false)
    }


    override fun initPresenter(): BasePresenter<out MvpView, out MvpRepository> {
        return SendConfirmationPresenter(this, SendConfirmationRepository(), SendConfirmationState())
    }
}