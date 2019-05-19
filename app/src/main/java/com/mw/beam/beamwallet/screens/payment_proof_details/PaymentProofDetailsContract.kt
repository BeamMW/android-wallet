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

package com.mw.beam.beamwallet.screens.payment_proof_details

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.PaymentProof

interface PaymentProofDetailsContract {

    interface View: MvpView {
        fun getPaymentProof(): PaymentProof
        fun init(paymentProof: PaymentProof)
        fun getDetailsContent(paymentProof: PaymentProof): String
        fun showCopiedAlert()
    }

    interface Presenter: MvpPresenter<View> {
        fun onCopyDetails()
        fun onCopyProof()
    }

    interface Repository: MvpRepository
}
