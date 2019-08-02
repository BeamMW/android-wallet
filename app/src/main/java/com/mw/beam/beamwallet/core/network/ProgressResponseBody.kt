package com.mw.beam.beamwallet.core.network

import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import io.reactivex.subjects.Subject
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException


class ProgressResponseBody(private val responseBody: ResponseBody, private val progressListener: Subject<OnSyncProgressData>): ResponseBody() {
    private var bufferedSource: BufferedSource? = null


    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var prevProgress = 0
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)

                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val percent = if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / responseBody.contentLength().toFloat() * 100

                val done = percent.toInt()
                if (done != prevProgress) {
                    prevProgress = done
                    progressListener.onNext(OnSyncProgressData(done, 100))
                }

                return bytesRead
            }
        }
    }
}