package com.mw.beam.beamwallet.core.listeners

import android.os.Handler
import android.os.Looper
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.helpers.prepareForLog
import com.mw.beam.beamwallet.core.utils.LogUtils
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject


/**
 * Created by vain onnellinen on 10/4/18.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object WalletListener {
    private var uiHandler = Handler(Looper.getMainLooper())
    private val DUMMY_OBJECT = Any()

    var subOnStatus: Subject<WalletStatus> = BehaviorSubject.create<WalletStatus>().toSerialized()
    var subOnTxStatus: Subject<OnTxStatusData> = BehaviorSubject.create<OnTxStatusData>().toSerialized()
    var subOnTxPeerUpdated: Subject<Array<TxPeer>> = BehaviorSubject.create<Array<TxPeer>>().toSerialized()
    var subOnSyncProgressUpdated: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnRecoverProgressUpdated: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnChangeCalculated: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnAllUtxoChanged: Subject<Array<Utxo>> = BehaviorSubject.create<Array<Utxo>>().toSerialized()
    var subOnAddresses: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnGeneratedNewAddress: Subject<WalletAddress> = BehaviorSubject.create<WalletAddress>().toSerialized()
    var subOnChangeCurrentWalletIDs: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnNodeConnectedStatusChanged: Subject<Boolean> = BehaviorSubject.create<Boolean>().toSerialized()
    var subOnNodeConnectionFailed: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()

    @JvmStatic
    fun onStatus(status: WalletStatus) = returnResult(subOnStatus, status, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onTxStatus(action: Int, tx: Array<TxDescription>?) = returnResult(subOnTxStatus, OnTxStatusData(action, tx), object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onTxPeerUpdated(peers: Array<TxPeer>?) = returnResult(subOnTxPeerUpdated, peers
            ?: emptyArray(), object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onSyncProgressUpdated(done: Int, total: Int) = returnResult(subOnSyncProgressUpdated, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onRecoverProgressUpdated(done: Int, total: Int, message: String) = returnResult(subOnRecoverProgressUpdated, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onChangeCalculated() = returnResult(subOnChangeCalculated, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onAllUtxoChanged(utxos: Array<Utxo>?) = returnResult(subOnAllUtxoChanged, utxos
            ?: emptyArray(), object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onAdrresses(own: Boolean, addresses: Array<WalletAddress>?) = returnResult(subOnAddresses, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onGeneratedNewAddress(addr: WalletAddress) = returnResult(subOnGeneratedNewAddress, addr, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onChangeCurrentWalletIDs() = returnResult(subOnChangeCurrentWalletIDs, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onNodeConnectedStatusChanged(isNodeConnected: Boolean) = returnResult(subOnNodeConnectedStatusChanged, isNodeConnected, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onNodeConnectionFailed() = returnResult(subOnNodeConnectionFailed, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    private fun <T> returnResult(subject: Subject<T>, result: T, responseName: String) {
        uiHandler.post {
            try {
                subject.onNext(result)

                when (result) {
                    DUMMY_OBJECT -> LogUtils.logResponse("null", responseName)
                    is Array<*> -> LogUtils.logResponse(if (result.isEmpty()) "null" else result.prepareForLog(), responseName)
                    else -> LogUtils.logResponse(result, responseName)
                }
            } catch (e: NullPointerException) {
                LogUtils.logErrorResponse(e, responseName)
            }
        }
    }
}
