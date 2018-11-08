package com.mw.beam.beamwallet.core.listeners

import android.os.Handler
import android.os.Looper
import com.mw.beam.beamwallet.core.entities.*
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
    var subOnTxPeerUpdated: Subject<Array<TxPeer>?> = BehaviorSubject.create<Array<TxPeer>?>().toSerialized()
    var subOnSyncProgressUpdated: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnChangeCalculated: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnAllUtxoChanged: Subject<Array<Utxo>> = BehaviorSubject.create<Array<Utxo>>().toSerialized()
    var subOnAddresses: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnGeneratedNewWalletID: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnChangeCurrentWalletIDs: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()

    @JvmStatic
    fun onStatus(status: WalletStatus) = returnResult(subOnStatus, status, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onTxStatus(action: Int, tx: Array<TxDescription>?) = returnResult(subOnTxStatus, OnTxStatusData(action, tx), object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onTxPeerUpdated(peers: Array<TxPeer>?) = returnResult(subOnTxPeerUpdated, peers, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onSyncProgressUpdated() = returnResult(subOnSyncProgressUpdated, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onChangeCalculated() = returnResult(subOnChangeCalculated, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onAllUtxoChanged(utxos: Array<Utxo>) = returnResult(subOnAllUtxoChanged, utxos, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onAddresses() = returnResult(subOnAddresses, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    fun onGeneratedNewWalletID() = returnResult(subOnGeneratedNewWalletID, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    @JvmStatic
    fun onChangeCurrentWalletIDs() = returnResult(subOnChangeCurrentWalletIDs, DUMMY_OBJECT, object {}.javaClass.enclosingMethod.name)

    private fun <T> returnResult(subject: Subject<T>, result: T, responseName: String) {
        uiHandler.post {
            try {
                subject.onNext(result)
                LogUtils.logResponse(result, responseName)
            } catch (e: NullPointerException) {
                LogUtils.logErrorResponse(e, responseName)
            }
        }
    }
}
