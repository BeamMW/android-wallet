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

package com.mw.beam.beamwallet.transaction_details

import android.view.Menu
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TxStatus

/**
 * Created by vain onnellinen on 10/18/18.
 */
interface TransactionDetailsContract {
    interface View : MvpView {
        fun getTransactionDetails(): TxDescription
        fun init(txDescription: TxDescription)
        fun configMenuItems(menu: Menu?, txStatus: TxStatus)
        fun finishScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun onMenuCreate(menu: Menu?)
        fun onCancelTransaction()
        fun onDeleteTransaction()
    }

    interface Repository : MvpRepository {
        var txDescription: TxDescription?
        fun deleteTransaction()
        fun cancelTransaction()
    }
}
