package com.mw.beam.beamwallet.core.network

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import com.mw.beam.beamwallet.core.entities.OnSyncProgressData
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit


interface MobileRestoreService {

    @GET("/masternet/masternet_recovery.bin") fun downloadMasternetRecoveryFile(): Observable<Response<ResponseBody>>
    @GET("/mainnet/mainnet_recovery.bin") fun downloadMainnetRecoveryFile(): Observable<Response<ResponseBody>>
    @GET("/testnet/testnet_recovery.bin") fun downloadTestnetRecoveryFile(): Observable<Response<ResponseBody>>
}

fun getOkHttpDownloadClientBuilder(progressListener: Subject<OnSyncProgressData>): OkHttpClient.Builder {
    val httpClientBuilder = OkHttpClient.Builder()

    // You might want to increase the timeout
    httpClientBuilder.connectTimeout(20, TimeUnit.SECONDS)
    httpClientBuilder.writeTimeout(0, TimeUnit.SECONDS)
    httpClientBuilder.readTimeout(5, TimeUnit.MINUTES)

    httpClientBuilder.addInterceptor { chain ->
        val originalResponse = chain.proceed(chain.request())
        originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body()!!, progressListener))
                .build()
    }

    return httpClientBuilder
}