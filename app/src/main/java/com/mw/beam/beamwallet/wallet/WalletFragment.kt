package com.mw.beam.beamwallet.wallet

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuPopupHelper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.TxPeer
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.EntitiesHelper
import kotlinx.android.synthetic.main.fragment_wallet.*


/**
 * Created by vain onnellinen on 10/1/18.
 */
class WalletFragment : BaseFragment<WalletPresenter>(), WalletContract.View {
    private lateinit var presenter: WalletPresenter
    private lateinit var adapter: TransactionsAdapter
    private var shouldExpandAvailable = false //TODO where should it be?
    private var shouldExpandInProgress = false

    companion object {
        fun newInstance(): WalletFragment {
            val args = Bundle()
            val fragment = WalletFragment()
            fragment.arguments = args

            return fragment
        }

        fun getFragmentTag(): String {
            return WalletFragment::class.java.simpleName
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_wallet, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = WalletPresenter(this, WalletRepository())
        configPresenter(presenter)
    }

    override fun configWalletStatus(walletStatus: WalletStatus) {
        available.text = EntitiesHelper.convertToBeam(walletStatus.available).toString()
    }

    override fun configTxStatus(txStatusData: OnTxStatusData) {
        if (txStatusData.tx != null) {
            adapter.setData(txStatusData.tx.sortedByDescending { it.modifyTime })
        }
    }

    override fun configTxPeerUpdated(peers: Array<TxPeer>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @SuppressLint("RestrictedApi")
    override fun init() {
        val context = context ?: return
        adapter = TransactionsAdapter(context, mutableListOf(), object : TransactionsAdapter.OnItemClickListener {
            override fun onItemClick(item: TxDescription) {
                (activity as TransactionDetailsHandler).onShowTransactionDetails(item)
            }
        })

        setTitle(getString(R.string.wallet_title))

        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = adapter

        btnReceive.setOnClickListener { presenter.onReceivePressed() }
        btnSend.setOnClickListener { presenter.onSendPressed() }

        btnExpandAvailable.setOnClickListener {
            animateDropDownIcon(btnExpandAvailable, shouldExpandAvailable)
            shouldExpandAvailable = !shouldExpandAvailable
            availableGroup.visibility = if (shouldExpandAvailable) View.GONE else View.VISIBLE
        }

        btnExpandInProgress.setOnClickListener {
            animateDropDownIcon(btnExpandInProgress, shouldExpandInProgress)
            shouldExpandInProgress = !shouldExpandInProgress
            inProgress.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
        }

        btnTransactionsMenu.setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(context, R.style.PopupMenu)
            val transactionsMenu = PopupMenu(wrapper, view)
            transactionsMenu.inflate(R.menu.wallet_transactions_menu)

            transactionsMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_search -> {
                        presenter.onSearchPressed()
                        true
                    }
                    R.id.menu_filter -> {
                        presenter.onFilterPressed()
                        true
                    }
                    R.id.menu_export -> {
                        presenter.onExportPressed()
                        true
                    }
                    R.id.menu_delete -> {
                        presenter.onDeletePressed()
                        true
                    }
                    else -> true
                }
            }

            val menuHelper = MenuPopupHelper(wrapper, transactionsMenu.menu as MenuBuilder, view)
            menuHelper.setForceShowIcon(true)
            menuHelper.gravity = Gravity.START
            menuHelper.show()
        }
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    interface TransactionDetailsHandler {
        fun onShowTransactionDetails(item: TxDescription)
    }
}
