package com.mw.beam.beamwallet.utxoDetails

import android.support.v4.content.ContextCompat
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.UtxoKeyType
import com.mw.beam.beamwallet.core.helpers.UtxoStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import com.mw.beam.beamwallet.core.helpers.toHex
import kotlinx.android.synthetic.main.activity_utxo_details.*
import kotlinx.android.synthetic.main.item_utxo.*

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsActivity : BaseActivity<UtxoDetailsPresenter>(), UtxoDetailsContract.View {
    private lateinit var presenter: UtxoDetailsPresenter

    companion object {
        const val EXTRA_UTXO_DETAILS = "EXTRA_UTXO_DETAILS"
    }

    override fun onControllerGetContentLayoutId() = R.layout.activity_utxo_details
    override fun getToolbarTitle(): String? = getString(R.string.utxo_details_title)
    override fun getUtxoDetails(): Utxo = intent.getParcelableExtra(EXTRA_UTXO_DETAILS)

    override fun init(utxo: Utxo) {
        configUtxoInfo(utxo)
        configUtxoDetails(utxo)
        configUtxoHistory(utxo)
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = UtxoDetailsPresenter(this, UtxoDetailsRepository())
        return presenter
    }

    private fun configUtxoInfo(utxo: Utxo) {
        when (utxo.statusEnum) {
            UtxoStatus.Available, UtxoStatus.Maturing, UtxoStatus.Incoming -> {
                ContextCompat.getColor(this, R.color.received_color).apply {
                    amount.setTextColor(this)
                    status.setTextColor(this)
                }
            }
            UtxoStatus.Outgoing, UtxoStatus.Change, UtxoStatus.Spent, UtxoStatus.Unavailable -> {
                ContextCompat.getColor(this, R.color.sent_color).apply {
                    amount.setTextColor(this)
                    status.setTextColor(this)
                }
            }
        }

        status.text = when (utxo.statusEnum) {
            UtxoStatus.Incoming, UtxoStatus.Change, UtxoStatus.Outgoing -> getString(R.string.utxo_status_in_progress)
            UtxoStatus.Maturing -> getString(R.string.utxo_status_maturing)
            UtxoStatus.Spent -> getString(R.string.utxo_status_spent)
            UtxoStatus.Available -> getString(R.string.utxo_status_available)
            UtxoStatus.Unavailable -> getString(R.string.utxo_status_unavailable)
        }

        detailedStatus.visibility = View.VISIBLE
        detailedStatus.text = when (utxo.statusEnum) {
            UtxoStatus.Incoming -> getString(R.string.utxo_status_incoming)
            UtxoStatus.Change -> getString(R.string.utxo_status_change)
            UtxoStatus.Outgoing -> getString(R.string.utxo_status_outgoing)
            UtxoStatus.Unavailable -> getString(R.string.utxo_status_result_rollback)
            UtxoStatus.Maturing, UtxoStatus.Spent, UtxoStatus.Available -> {
                detailedStatus.visibility = View.GONE
                null //TODO add correct description for maturing
            }
        }

        amount.text = utxo.amount.convertToBeam()
        id.text = utxo.id.toString() //TODO implement correct id from API
    }

    private fun configUtxoDetails(utxo: Utxo) {
        transactionId.text = utxo.createTxId.toHex()

        utxoType.text = when (utxo.keyTypeEnum) {
            UtxoKeyType.Commission -> getString(R.string.utxo_type_commission)
            UtxoKeyType.Coinbase -> getString(R.string.utxo_type_coinbase)
            UtxoKeyType.Regular -> getString(R.string.utxo_type_regular)
            UtxoKeyType.Change -> getString(R.string.utxo_type_change)
            UtxoKeyType.Kernel -> getString(R.string.utxo_type_kernel)
            UtxoKeyType.Kernel2 -> getString(R.string.utxo_type_kernel2)
            UtxoKeyType.Identity -> getString(R.string.utxo_type_identity)
            UtxoKeyType.ChildKey -> getString(R.string.utxo_type_childKey)
            UtxoKeyType.Bbs -> getString(R.string.utxo_type_bbs)
            UtxoKeyType.Decoy -> getString(R.string.utxo_type_decoy)
        }
    }

    private fun configUtxoHistory(utxo: Utxo) {

    }
}
