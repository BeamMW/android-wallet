package com.mw.beam.beamwallet.core.network

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface MobileRestoreService {

    @GET("/masternet/masternet_recovery.bin") fun downloadMasternetRecoveryFile(): Observable<Response<ResponseBody>>
    @GET("/mainnet/mainnet_recovery.bin") fun downloadMainnetRecoveryFile(): Observable<Response<ResponseBody>>
    @GET("/testnet/testnet_recovery.bin") fun downloadTestnetRecoveryFile(): Observable<Response<ResponseBody>>
}