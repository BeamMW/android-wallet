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

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PaymentProofDetailsPresenter(view: PaymentProofDetailsContract.View, repository: PaymentProofDetailsContract.Repository, private val state: PaymentProofDetailsState)
    : BasePresenter<PaymentProofDetailsContract.View, PaymentProofDetailsContract.Repository>(view, repository), PaymentProofDetailsContract.Presenter {
    private val COPY_TAG = "PROOF"

    override fun onViewCreated() {
        super.onViewCreated()
        state.paymentProof = view?.getPaymentProof()
        if (state.paymentProof != null) {
            view?.init(state.paymentProof!!)
        }
    }

    override fun onCopyDetails() {
        if (state.paymentProof != null) {
            view?.copyToClipboard(view?.getDetailsContent(state.paymentProof!!), COPY_TAG)
            view?.showCopiedAlert()
        }
    }

    override fun onCopyProof() {
        view?.copyToClipboard(state.paymentProof?.rawProof, COPY_TAG)
        view?.showCopiedAlert()
    }

}