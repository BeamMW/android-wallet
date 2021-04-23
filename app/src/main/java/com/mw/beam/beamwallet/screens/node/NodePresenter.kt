package com.mw.beam.beamwallet.screens.node

import com.mw.beam.beamwallet.base_screen.BasePresenter


class NodePresenter(view: NodeContract.View?, repository: NodeContract.Repository, val state: NodeState)
    : BasePresenter<NodeContract.View, NodeContract.Repository>(view, repository), NodeContract.Presenter {

    private val copyTag = "OwnerKey"

    override fun initSubscriptions() {
        super.initSubscriptions()

    }

   // override fun getSubscriptions(): Array<Disposable>? = arrayOf(keyDisposable)
}