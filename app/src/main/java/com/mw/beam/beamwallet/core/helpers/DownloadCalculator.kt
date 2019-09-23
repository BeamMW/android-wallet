package com.mw.beam.beamwallet.core.helpers

import java.util.concurrent.TimeUnit

object DownloadCalculator {

    private val kFilterRange = 10
    private val kSecondsInMinute = 60.0
    private val kSecondsInHour = 60.0 * 60.0
    private val kMaxEstimate = 4 * kSecondsInHour

    private var m_total: Int = 0
    private var m_done: Int = 0
    private var m_lastDone: Int = 0
    private var m_estimate: Int = 0

    private var m_isDownloadStarted = false
    private var m_startTimestamp = 0L
    private var m_previousUpdateTimestamp = 0L
    private var m_lastUpdateTimestamp = 0L

    private var m_bpsWindowedFilter = mutableListOf<Double>()
    private var m_bpsWholeTimeFilter = mutableListOf<Double>()
    private var m_estimateFilter = mutableListOf<Double>()

    private var m_bpsWindowedFilter1 = mutableListOf<Double>()
    private var m_bpsWholeTimeFilter1 = mutableListOf<Double>()
    private var m_estimateFilter1 = mutableListOf<Double>()

    fun onStopDownload() {
        m_isDownloadStarted = false
    }

    fun onStartDownload() {
        if (!m_isDownloadStarted)
        {
            m_previousUpdateTimestamp = 0L
            m_lastUpdateTimestamp = 0L

            m_done = 0
            m_total = 0

            m_startTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

            m_bpsWindowedFilter.clear()
            m_bpsWholeTimeFilter.clear()
            m_estimateFilter.clear()

            m_bpsWindowedFilter1.clear()
            m_bpsWholeTimeFilter1.clear()
            m_estimateFilter1.clear()

            m_isDownloadStarted = true
        }
    }

    fun onCalculateTime(done:Int, total:Int):Int? {
        return if (m_isDownloadStarted && total > 0) {
            m_previousUpdateTimestamp = m_lastUpdateTimestamp;
            m_lastUpdateTimestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            m_lastDone = m_done
            m_done = done
            m_total = total

            val wbps = getWindowedBps()
            val bps = (getWholeTimeBps() + wbps) / 2

            m_estimate = getEstimate(bps)

//            if (m_bpsWindowedFilter.count() < 5) {
//                return null
//            }

            return m_estimate
        }
        else null
    }

    private fun getWindowedBps():Double {
        if (m_done == 0)
            return 0.0

       var timeDiff = when(m_previousUpdateTimestamp >0L) {
            true -> m_lastUpdateTimestamp - m_previousUpdateTimestamp
            false -> m_lastUpdateTimestamp - m_startTimestamp
       }

        if (timeDiff < 1)
            timeDiff = 1

        val sample = (m_done - m_lastDone) / timeDiff.toDouble()

        if (m_bpsWindowedFilter.count() >= kFilterRange * 3) {
            m_bpsWindowedFilter1.add(sample)

            var index = 0
            m_bpsWindowedFilter1.forEach {
                m_bpsWindowedFilter[index] = it
                index += 1
            }

            if (m_bpsWindowedFilter1.count() >= kFilterRange * 3) {
                m_bpsWindowedFilter1.clear()
            }
        }
        else{
            m_bpsWindowedFilter.add(sample)
        }

        return m_bpsWindowedFilter.average()
    }

    private fun getWholeTimeBps():Double {
        if (m_done == 0)
            return 0.0

        val timeDiff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - m_startTimestamp + 1
        val sample = m_done / timeDiff.toDouble()

        if (m_bpsWholeTimeFilter.count() >= kFilterRange) {
            m_bpsWholeTimeFilter1.add(sample)

            var index = 0
            m_bpsWholeTimeFilter1.forEach {
                m_bpsWholeTimeFilter[index] = it
                index += 1
            }

            if (m_bpsWholeTimeFilter1.count() >= kFilterRange) {
                m_bpsWholeTimeFilter1.clear()
            }
        }
        else{
            m_bpsWholeTimeFilter.add(sample)
        }

        return median(m_bpsWholeTimeFilter)
    }

    private fun getEstimate(bps:Double):Int {
        val sample = (m_total - m_done) / bps

        if (m_estimateFilter.count() >= kFilterRange) {
            m_estimateFilter1.add(sample)

            var index = 0
            m_estimateFilter1.forEach {
                m_estimateFilter[index] = it
                index += 1
            }

            if (m_estimateFilter1.count() >= kFilterRange) {
                m_estimateFilter1.clear()
            }
        }
        else{
            m_estimateFilter.add(sample)
        }

        val estimate = median(m_estimateFilter)

        return when {
            estimate > kMaxEstimate -> kMaxEstimate.toInt()
            estimate < 2 * kSecondsInMinute -> kotlin.math.ceil(m_estimateFilter.average()).toInt()
            else -> estimate.toInt()
        }
    }

    private  fun <T : Number> median(numbers: Collection<T>): Double where T : Comparable<T> {
        if (numbers.isEmpty()) {
            throw IllegalArgumentException("Cannot compute median on empty collection of numbers")
        }
        val numbersList = ArrayList(numbers)
        numbersList.sort()
        val middle = numbersList.size / 2
        return if (numbersList.size % 2 == 0) {
            0.5 * (numbersList[middle].toDouble() + numbersList[middle - 1].toDouble())
        } else {
            numbersList[middle].toDouble()
        }
    }
}