package com.mw.beam.beamwallet.core.helpers

import io.reactivex.disposables.Disposable

class EmptyDisposable: Disposable {
    private var isDisposed = false

    override fun isDisposed(): Boolean {
        return isDisposed
    }

    override fun dispose() {
        isDisposed = true
    }
}