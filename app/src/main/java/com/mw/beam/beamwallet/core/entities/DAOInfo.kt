package com.mw.beam.beamwallet.core.entities

data class DAOInfo (
    val comment:String?,
    val fee:String?,
    val feeRate:String?,
    val isEnough:Boolean?,
    val isSpend:Boolean?,
    val rateUnit:String?,
    /** When isSpend is true, the asset id to check wallet balance for (source). If null, amounts[].assetID is used (may be target). */
    val sourceAssetId: Int? = null,
)