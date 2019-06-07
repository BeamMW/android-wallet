package com.mw.beam.beamwallet.core.helpers

import android.os.AsyncTask

class DelayedTask : AsyncTask<Void, Int, Boolean>() {
    private var duration: Int = 0
    private var task: (() -> Unit)? = null
    private var onProgress: ((Int) -> Unit)? = null
    private var onResult: ((Boolean) -> Unit)? = null

    companion object {
        fun startNew(durationSecond: Int, task: () -> Unit, onProgress: ((Int) -> Unit)? = null, onResult: ((Boolean) -> Unit)? = null): DelayedTask {
            return DelayedTask().apply {
                this.task = task
                this.onProgress = onProgress
                this.onResult = onResult
                duration = durationSecond
                execute()
            }
        }
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        publishProgress(duration)
        while (duration > 0 && !isCancelled) {
            Thread.sleep(1000)
            if (!isCancelled) {
                duration--
                publishProgress(duration)
            }
        }

        return !isCancelled
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        val isRunningTask = if (!isCancelled) {
            task?.invoke()
            true
        } else {
            false
        }

        onResult?.invoke(isRunningTask)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        values[0]?.let { onProgress?.invoke(it) }
    }
}