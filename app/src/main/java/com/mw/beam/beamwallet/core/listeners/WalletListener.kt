/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */
package com.mw.beam.beamwallet.core.listeners

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mw.beam.beamwallet.BuildConfig
import com.mw.beam.beamwallet.core.App
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.*
import com.mw.beam.beamwallet.core.entities.dto.*
import com.mw.beam.beamwallet.core.helpers.*
import com.mw.beam.beamwallet.core.utils.LogUtils
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


/**
 *  10/4/18.
 */
object WalletListener {
    private var uiHandler = Handler(Looper.getMainLooper())
    private val DUMMY_OBJECT = Any()

    var oldCurrent = -1

    var subOnStatus: Subject<WalletStatus> = BehaviorSubject.create<WalletStatus>().toSerialized()

    private var subOnTxStatus: Subject<OnTxStatusData> = BehaviorSubject.create<OnTxStatusData>().toSerialized()
    val obsOnTxStatus: Observable<OnTxStatusData> = subOnTxStatus.map {
        it.tx?.forEach { tx ->
            if (!tx.sender.value) {
                val savedComment = if (it.action == ChangeAction.ADDED || it.action == ChangeAction.UPDATED)
                    ReceiveTxCommentHelper.getSavedCommnetAndSaveForTx(tx)
                else ReceiveTxCommentHelper.getSavedComment(tx)

                tx.message = if (savedComment.isNotBlank()) savedComment else ""
            }
        }
        it
    }

    var obsOnUtxos: Subject<OnUTXOData> = BehaviorSubject.create<OnUTXOData>().toSerialized()
//    var obsOnAddresses: Subject<OnAddressesDataWithAction> = BehaviorSubject.create<OnAddressesDataWithAction>().toSerialized()
    var subOnAllUtxoChanged: Subject<List<Utxo>> = BehaviorSubject.create<List<Utxo>>().toSerialized()

    var subOnSyncProgressUpdated: Subject<OnSyncProgressData> = BehaviorSubject.create<OnSyncProgressData>().toSerialized()
    var subOnNodeSyncProgressUpdated: Subject<OnSyncProgressData> = BehaviorSubject.create<OnSyncProgressData>().toSerialized()
    var subOnChangeCalculated: Subject<Long> = BehaviorSubject.create<Long>().toSerialized()
    var subOnAddresses: Subject<OnAddressesData> = BehaviorSubject.create<OnAddressesData>().toSerialized()
    var subOnAddressesChanged: Subject<OnAddressesDataWithAction> = BehaviorSubject.create<OnAddressesDataWithAction>().toSerialized()
    var subOnGeneratedNewAddress: Subject<WalletAddress> = BehaviorSubject.create<WalletAddress>().toSerialized()
    var subOnNodeConnectedStatusChanged: Subject<Boolean> = BehaviorSubject.create<Boolean>().toSerialized()
    var subOnNodeConnectionFailed: Subject<NodeConnectionError> = PublishSubject.create<NodeConnectionError>().toSerialized()
    var subOnCantSendToExpired: Subject<Any> = BehaviorSubject.create<Any>().toSerialized()
    var subOnStartedNode: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnStoppedNode: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnNodeCreated: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnNodeThreadFinished: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnFailedToStartNode: Subject<Any> = PublishSubject.create<Any>().toSerialized()
    var subOnPaymentProofExported: Subject<PaymentProof> = BehaviorSubject.create<PaymentProof>().toSerialized()
    var subOnCoinsByTx: Subject<List<Utxo>?> = BehaviorSubject.create<List<Utxo>?>().toSerialized()
    val subOnImportRecoveryProgress = PublishSubject.create<OnSyncProgressData>().toSerialized()
    var subOnDataExported: Subject<String> = PublishSubject.create<String>().toSerialized()
    var subOnDataImported: Subject<Boolean> = PublishSubject.create<Boolean>().toSerialized()
    var subOnExchangeRates: Subject<List<ExchangeRate>?> = BehaviorSubject.create<List<ExchangeRate>?>().toSerialized()
    var subNotificationChanged: Subject<OnNotificationDataWithAction> = BehaviorSubject.create<OnNotificationDataWithAction>().toSerialized()

    @JvmStatic
    fun onStatus(status: WalletStatusDTO) : Unit {
        if (App.isAuthenticated) {
            return returnResult(subOnStatus, WalletStatus(status), "onStatus")
        }
        else{
            subOnStatus.onNext(WalletStatus(status))
        }
    }

    @JvmStatic
    fun onTxStatus(action: Int, tx: Array<TxDescriptionDTO>?) = returnResult(subOnTxStatus, OnTxStatusData(ChangeAction.fromValue(action), tx?.map { TxDescription(it) }), "onTxStatus")

    @JvmStatic
    fun onSyncProgressUpdated(done: Int, total: Int) = returnResult(subOnSyncProgressUpdated, OnSyncProgressData(done, total), "onSyncProgressUpdated")

    @JvmStatic
    fun onNodeSyncProgressUpdated(done: Int, total: Int) = returnResult(subOnNodeSyncProgressUpdated, OnSyncProgressData(done, total), "onNodeSyncProgressUpdated")

    @JvmStatic
    fun onChangeCalculated(amount: Long) = returnResult(subOnChangeCalculated, amount, "onChangeCalculated")

    @JvmStatic
    fun onAllUtxoChanged(action: Int, utxos: Array<UtxoDTO>?) = returnResult(obsOnUtxos, OnUTXOData(ChangeAction.fromValue(action), utxos?.map { Utxo(it) }), "onAllUtxoChanged")

    @JvmStatic
    fun onAllUtxoChanged(utxos: Array<UtxoDTO>?) : Unit {
        if (App.isAuthenticated) {
            return returnResult(subOnAllUtxoChanged, utxos?.map { Utxo(it) }
                    ?: emptyList(), "onAllUtxoChanged")
        }
    }

    @JvmStatic
    fun onAddressesChanged(action: Int, addresses: Array<WalletAddressDTO>?) = returnResult(subOnAddressesChanged, OnAddressesDataWithAction(ChangeAction.fromValue(action), addresses?.map { WalletAddress(it) }), "onAddressesChanged")

    @JvmStatic
    fun onAddresses(own: Boolean, addresses: Array<WalletAddressDTO>?) = returnResult(subOnAddresses, OnAddressesData(own, addresses?.map { WalletAddress(it) }), "onAddresses")

    @JvmStatic
    fun onGeneratedNewAddress(addr: WalletAddressDTO) = returnResult(subOnGeneratedNewAddress, WalletAddress(addr), "onGeneratedNewAddress")

    @JvmStatic
    fun onNodeConnectedStatusChanged(isNodeConnected: Boolean) = returnResult(subOnNodeConnectedStatusChanged, isNodeConnected, "onNodeConnectedStatusChanged")

    @JvmStatic
    fun onNodeConnectionFailed(error : Int) = returnResult(subOnNodeConnectionFailed, NodeConnectionError.fromValue(error), "onNodeConnectionFailed")

    @JvmStatic
    fun onCantSendToExpired() = returnResult(subOnCantSendToExpired, DUMMY_OBJECT, "onCantSendToExpired")

    @JvmStatic
    fun onStartedNode() = returnResult(subOnStartedNode, DUMMY_OBJECT, "onStartedNode")

    @JvmStatic
    fun onStoppedNode() = returnResult(subOnStoppedNode, DUMMY_OBJECT, "onStoppedNode")

    @JvmStatic
    fun onNodeCreated() = returnResult(subOnNodeCreated, DUMMY_OBJECT, "onNodeCreated")

    @JvmStatic
    fun onNodeThreadFinished() {
        subOnNodeThreadFinished.onNext(DUMMY_OBJECT)
        //returnResult(subOnNodeThreadFinished, DUMMY_OBJECT, "onNodeThreadFinished")
    }

    @JvmStatic
    fun onFailedToStartNode() = returnResult(subOnFailedToStartNode, DUMMY_OBJECT, "onFailedToStartNode")

    @JvmStatic
    fun onPaymentProofExported(txId: String, proof: PaymentInfoDTO) = returnResult(subOnPaymentProofExported, PaymentProof(txId, proof), "onPaymentProofExported")

    @JvmStatic
    fun onCoinsByTx(utxos: Array<UtxoDTO>?) = returnResult(subOnCoinsByTx, utxos?.map { Utxo(it) }, "onCoinsByTx")

    @JvmStatic
    fun onImportRecoveryProgress(done: Long, total: Long) {
        val current = ((done.toFloat() / total) * 100).toInt()

        if (current!=oldCurrent) {
            val time = DownloadCalculator.onCalculateTime(done.toInt(),total.toInt())

            oldCurrent = current

            LogUtils.logResponse(current.toString() + " ESTIMATE: " + time?.toTimeFormat(App.self), "onImportRecoveryProgress")

            uiHandler.post {
                subOnImportRecoveryProgress.onNext(OnSyncProgressData(current, 100, time))
            }
        }
    }

    @JvmStatic
    fun onImportDataFromJson(isOk: Boolean) {
        subOnDataImported.onNext(isOk)
        LogUtils.logResponse(isOk, "onImportDataFromJson")
    }

    @JvmStatic
    fun onExportDataToJson(data: String) {
        subOnDataExported.onNext(data)
        LogUtils.logResponse(data, "onExportDataToJson")
    }

    @JvmStatic
    fun onNewVersionNotification(action: Int, notificationInfo: NotificationDTO, content: VersionInfoDTO) {
        if(content.application == 1) {
            val versionName: String = BuildConfig.VERSION_NAME
            val versionReceivedName: String =  content.versionMajor.toString() + "." + content.versionMinor.toString()

            if (versionReceivedName.toDouble() > versionName.toDouble()) {
                val notification = Notification(
                        NotificationType.Version,
                        notificationInfo.id,
                        "v." + content.versionMajor.toString() + "." + content.versionMinor.toString(),
                        notificationInfo.state == 1,
                        false, notificationInfo.createTime,
                        "")
                subNotificationChanged.onNext(OnNotificationDataWithAction(ChangeAction.fromValue(action), notification))
            }
        }
        else {
            AppManager.instance.deleteNotification(notificationInfo.id)
        }
    }

    @JvmStatic
    fun onAddressChangedNotification(action: Int, notificationInfo: NotificationDTO, content: WalletAddressDTO) {
        val address =  WalletAddress(content)
        if(address.isExpired) {
            val notification = Notification(
                    NotificationType.Address,
                    notificationInfo.id,
                    content.walletID,
                    notificationInfo.state == 1,
                    false, notificationInfo.createTime,
                    "")
            subNotificationChanged.onNext(OnNotificationDataWithAction(ChangeAction.fromValue(action), notification))
        }
        else {
            AppManager.instance.deleteNotification(notificationInfo.id)
        }
    }

    @JvmStatic
    fun onTransactionFailedNotification(action: Int, notificationInfo: NotificationDTO, content: TxDescriptionDTO) {
        val notification = Notification(
                NotificationType.Transaction,
                notificationInfo.id,
                content.id,
                notificationInfo.state == 1,
                false, notificationInfo.createTime,
                "")
        subNotificationChanged.onNext(OnNotificationDataWithAction(ChangeAction.fromValue(action), notification))
    }

    @JvmStatic
    fun onTransactionCompletedNotification(action: Int, notificationInfo: NotificationDTO, content: TxDescriptionDTO) {
        val notification = Notification(
                NotificationType.Transaction,
                notificationInfo.id,
                content.id,
                notificationInfo.state == 1,
                false, notificationInfo.createTime,
                "")
        subNotificationChanged.onNext(OnNotificationDataWithAction(ChangeAction.fromValue(action), notification))
    }

    @JvmStatic
    fun onBeamNewsNotification(action: Int) {
        println(">>>>>>>>>>>>>> async onBeamNewsNotification in Java")
    }

    @JvmStatic
    fun onExchangeRates(rates: Array<ExchangeRateDTO>?) {
        LogUtils.logResponse(rates, "onExchangeRates")
        if(rates!=null) {
            subOnExchangeRates.onNext(rates.map { ExchangeRate(it)})
        }
    }

    private fun <T> returnResult(subject: Subject<T>, result: T, responseName: String) {
        uiHandler.post {
            try {
                subject.onNext(result)

                when (result) {
                    DUMMY_OBJECT -> LogUtils.logResponse("null", responseName)
                    is Array<*> -> LogUtils.logResponse(if (result.isEmpty()) "null" else result.toList().prepareForLog(), responseName)
                    else -> LogUtils.logResponse(result, responseName)
                }
            } catch (e: Exception) {
                LogUtils.logErrorResponse(e, responseName)
            }
        }
    }
}
