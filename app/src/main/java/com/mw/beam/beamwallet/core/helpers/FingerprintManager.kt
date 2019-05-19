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

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


/**
 * Created by vain onnellinen on 4/25/19.
 */
object FingerprintManager {
    private const val KEYSTORE_TYPE = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "Beam Wallet"
    private var keyStore: KeyStore? = null
    var cryptoObject: FingerprintManagerCompat.CryptoObject? = null

    init {
        configKeystore()

        if (keyStore != null && isKeyReady()) {
            initCipher()
        }
    }

    fun isManagerAvailable(): Boolean {
        return cryptoObject != null
    }

    fun checkSensorState(context: Context): SensorState {
        val fingerprintManager = FingerprintManagerCompat.from(context)

        if (fingerprintManager.isHardwareDetected) {
            val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (!keyguardManager.isKeyguardSecure) {
                return SensorState.NOT_BLOCKED
            }

            return if (!fingerprintManager.hasEnrolledFingerprints()) {
                SensorState.NO_FINGERPRINTS
            } else SensorState.READY
        } else {
            return SensorState.NOT_SUPPORTED
        }
    }

    private fun isKeyReady(): Boolean {
        try {
            return keyStore?.containsAlias(KEYSTORE_ALIAS) ?: false || generateKey()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }

        return false
    }

    private fun configKeystore() {
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            keyStore?.load(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateKey(): Boolean {
        return try {
            keyStore?.load(null)

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
            keyGenerator.init(KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())

            keyGenerator.generateKey()
            true
        } catch (exc: Exception) {
            exc.printStackTrace()
            false
        }
    }

    private fun initCipher() {
        var cipher: Cipher?

        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        return try {
            keyStore?.load(null)

            val key = keyStore?.getKey(KEYSTORE_ALIAS, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
        } catch (e: KeyPermanentlyInvalidatedException) {
            deleteInvalidKey()
        } catch (e: Exception) {
        }
    }

    private fun deleteInvalidKey() {
        try {
            keyStore?.deleteEntry(KEYSTORE_ALIAS)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
    }

    enum class SensorState {
        NOT_SUPPORTED,
        NOT_BLOCKED, // no pin or pass added to device
        NO_FINGERPRINTS, // no fingerprints are added
        READY
    }
}
