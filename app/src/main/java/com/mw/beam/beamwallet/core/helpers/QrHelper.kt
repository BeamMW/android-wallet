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

import android.graphics.Bitmap
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


/**
 *  2/12/19.
 */
object QrHelper {
    const val BEAM_URI_PREFIX = "beam://"
    const val BEAM_QR_PREFIX = "beam:"
    private const val AMOUNT_PARAMETER = "amount"
    const val MAX_TOKEN_LENGTH = 80
    const val MIN_TOKEN_LENGTH = 60

    val tokenRegex = Regex("[^A-Fa-f0-9]")
    private val amountDecimalFormat = DecimalFormat().apply {
        decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = '.'
            groupingSeparator = ' '
        }
        maximumIntegerDigits = 309
        maximumFractionDigits = 1074
    }

    fun isNewQrVersion(text: String) = text.startsWith(BEAM_QR_PREFIX)

  //  fun isValidAddress(address: String) = address == address.replace(tokenRegex, "") && address.isNotBlank() && address.length <= MAX_TOKEN_LENGTH && address.length >= MIN_TOKEN_LENGTH

    fun getScannedAddress(text: String): String {
        return if (text.startsWith(BEAM_QR_PREFIX)) {
            val removePrefix = text.removePrefix(BEAM_QR_PREFIX)

            if (removePrefix.contains("?")) {
                removePrefix.substring(0, removePrefix.indexOf("?"))
            } else {
                removePrefix
            }

        } else {
            text
        }
    }

    fun createQrString(receiveToken: String, amount: Double?) = "$BEAM_QR_PREFIX$receiveToken" +
            if (amount != null) "?$AMOUNT_PARAMETER=${amountDecimalFormat.format(amount).replace(" ", "")}" else ""

    fun parseQrCode(text: String): QrObject {
        val uri = Uri.parse(text.replace(BEAM_QR_PREFIX, BEAM_URI_PREFIX))
        val amount = uri.getQueryParameter(AMOUNT_PARAMETER)?.toDoubleOrNull()

        return QrObject(getScannedAddress(text), amount)
    }

    @Throws(WriterException::class, NullPointerException::class)
    fun textToImage(text: String, width: Int, height: Int, darkColor: Int, lightColor: Int): Bitmap? {
        val bitMatrix: BitMatrix

        try {
            val hintMap = HashMap<EncodeHintType, Any>()
            hintMap[EncodeHintType.MARGIN] = 0
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q

            bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hintMap)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth
            for (x in 0 until bitMatrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) lightColor else darkColor
            }
        }

        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, width, 0, 0, bitMatrixWidth, bitMatrixHeight)

        return bitmap
    }

    data class QrObject(val address: String, val amount: Double? = null)
}
