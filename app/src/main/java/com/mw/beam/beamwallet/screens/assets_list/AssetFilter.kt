package com.mw.beam.beamwallet.screens.assets_list

enum class AssetFilter {
    recent_old, old_recent, amount_small_large, amount_large_small, amount_usd_small, amount_usd_large;

    companion object {
        private val map: HashMap<Int, AssetFilter> = HashMap()

        init {
            AssetFilter.values().forEach {
                map[it.ordinal] = it
            }
        }

        fun fromValue(type: Int): AssetFilter {
            return map[type] ?: AssetFilter.recent_old
        }
    }
}