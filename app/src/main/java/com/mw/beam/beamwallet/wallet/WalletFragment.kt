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
import android.view.View
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.baseScreen.BaseFragment
import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.TxPeer
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import com.mw.beam.beamwallet.core.helpers.convertToBeamWithSign
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
        fun newInstance() = WalletFragment().apply { arguments = Bundle() }
        fun getFragmentTag(): String = WalletFragment::class.java.simpleName
    }

    override fun onControllerGetContentLayoutId() = R.layout.fragment_wallet

    override fun configWalletStatus(walletStatus: WalletStatus) {
        available.text = walletStatus.available.convertToBeam().toString()
    }

    override fun configInProgress(receivingAmount: Long, sendingAmount: Long, maturingAmount: Long) {
        when (receivingAmount) {
            0L -> receivingGroup.visibility = View.GONE
            else -> {
                receiving.text = receivingAmount.convertToBeamWithSign(false)
                receivingGroup.visibility = View.GONE
                receivingGroup.visibility = View.VISIBLE
            }
        }

        when (sendingAmount) {
            0L -> sendingGroup.visibility = View.GONE
            else -> {
                sending.text = sendingAmount.convertToBeamWithSign(true)
                sendingGroup.visibility = View.VISIBLE
            }
        }

        when (maturingAmount) {
            0L -> maturingGroup.visibility = View.GONE
            else -> {
                maturing.text = maturingAmount.convertToBeam().toString()
                maturingGroup.visibility = View.VISIBLE
            }
        }
    }

    override fun configTxStatus(txStatusData: OnTxStatusData) {
        if (txStatusData.tx != null) {
            adapter.setData(txStatusData.tx.sortedByDescending { it.modifyTime })
        }
    }

    override fun configTxPeerUpdated(peers: Array<TxPeer>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun init() {
        setTitle(getString(R.string.wallet_title))
        initTransactionsList()
    }

    @SuppressLint("RestrictedApi")
    override fun addListeners() {
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
            inProgressGroup.visibility = if (shouldExpandInProgress) View.GONE else View.VISIBLE
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

    private fun initTransactionsList() {
        val context = context ?: return

        adapter = TransactionsAdapter(context, mutableListOf(), object : TransactionsAdapter.OnItemClickListener {
            override fun onItemClick(item: TxDescription) {
                presenter.onTransactionPressed(item)
            }
        })

        transactionsList.layoutManager = LinearLayoutManager(context)
        transactionsList.adapter = adapter
    }

    override fun showTransactionDetails(txDescription: TxDescription) {
        (activity as TransactionDetailsHandler).onShowTransactionDetails(txDescription)
    }

    override fun showReceiveScreen() {
        (activity as TransactionDetailsHandler).onReceive()
    }

    override fun showSendScreen() {
        (activity as TransactionDetailsHandler).onSend()
    }

    override fun clearListeners() {
        btnReceive.setOnClickListener(null)
        btnSend.setOnClickListener(null)
        btnExpandAvailable.setOnClickListener(null)
        btnExpandInProgress.setOnClickListener(null)
        btnTransactionsMenu.setOnClickListener(null)
    }

    private fun animateDropDownIcon(view: View, shouldExpand: Boolean) {
        val angleFrom = if (shouldExpand) 180f else 360f
        val angleTo = if (shouldExpand) 360f else 180f
        val anim = ObjectAnimator.ofFloat(view, "rotation", angleFrom, angleTo)
        anim.duration = 500
        anim.start()
    }

    override fun initPresenter(): BasePresenter<out MvpView> {
        presenter = WalletPresenter(this, WalletRepository())
        return presenter
    }

    interface TransactionDetailsHandler {
        fun onShowTransactionDetails(item: TxDescription)
        fun onReceive()
        fun onSend()
    }
}
