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

package com.mw.beam.beamwallet.core.helpers

import com.mw.beam.beamwallet.core.entities.TxDescription

object ReceiveTxCommentHelper {

    fun saveCommentToAddress(address: String, comment: String) {
        val receiveCommentKey = "receive_comment:$address"
        PreferencesManager.putString(receiveCommentKey, comment)
    }

    fun getSavedComment(tx: TxDescription): String {
        val txCommentKey = "tx_comment:${tx.id}"
        val s = PreferencesManager.getString(txCommentKey) ?: ""
        return s
    }

    fun getSavedCommnetAndSaveForTx(tx: TxDescription): String {
        val receiveCommentKey = "receive_comment:${tx.myId}"
        val comment = PreferencesManager.getString(receiveCommentKey) ?: ""
        return comment
    }
}