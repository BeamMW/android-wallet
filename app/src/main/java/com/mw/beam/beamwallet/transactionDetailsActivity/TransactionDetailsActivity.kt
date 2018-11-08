package com.mw.beam.beamwallet.transactionDetailsActivity

import android.os.Bundle
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseActivity
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.transactionDetails.TransactionDetailsFragment
import kotlinx.android.synthetic.main.activity_transaction_details.*


/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsActivity : BaseActivity<TransactionDetailsActivityPresenter>(), TransactionDetailsActivityContract.View {
    private lateinit var presenter: TransactionDetailsActivityPresenter

    companion object {
        const val EXTRA_TRANSACTION_DETAILS = "EXTRA_TRANSACTION_DETAILS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        presenter = TransactionDetailsActivityPresenter(this, TransactionDetailsActivityRepository())
        configPresenter(presenter)
    }

    override fun showTransactionDetailsFragment(txDescription: TxDescription) {
        showFragment(TransactionDetailsFragment.newInstance(txDescription), TransactionDetailsFragment.getFragmentTag(), null, false)
    }

    override fun getTransactionDetails(): TxDescription {
        return intent.getParcelableExtra(EXTRA_TRANSACTION_DETAILS)
    }

    override fun init() {
        initToolbar(toolbar, getString(R.string.transaction_details_title))
    }
}
