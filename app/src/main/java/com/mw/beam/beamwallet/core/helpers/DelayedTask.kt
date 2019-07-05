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