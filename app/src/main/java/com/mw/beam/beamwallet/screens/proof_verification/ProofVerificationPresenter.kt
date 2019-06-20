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

package com.mw.beam.beamwallet.screens.proof_verification

import com.mw.beam.beamwallet.base_screen.BasePresenter

class ProofVerificationPresenter(view: ProofVerificationContract.View?, repository: ProofVerificationContract.Repository, private val state: ProofVerificationState)
    : BasePresenter<ProofVerificationContract.View, ProofVerificationContract.Repository>(view, repository), ProofVerificationContract.Presenter {
    private val COPY_TAG = "PROOF_VERIFICATION"

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onProofCodeChanged(proof: String) {
        if (proof.isEmpty()) {
            view?.clear()
            view?.hideErrorProof()
            return
        }

        state.proof = repository.getVerifyPaymentProof(proof)

        if (state.proof?.isValid == true) {
            view?.hideErrorProof()
            state.proof?.let { view?.showProof(it) }
        } else {
            view?.showErrorProof()
            view?.clear()
        }
    }

    override fun onCopyDetailsPressed() {
        view?.copyToClipboard(view?.getDetailsContent(state.proof ?: return), COPY_TAG)
        view?.showCopiedMessage()
    }
}