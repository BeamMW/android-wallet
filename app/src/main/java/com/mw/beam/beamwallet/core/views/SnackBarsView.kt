package com.mw.beam.beamwallet.core.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mw.beam.beamwallet.R
import java.util.*
import kotlin.concurrent.schedule

class SnackBarsView: FrameLayout {
    private val snackbarLifeTime: Long = 5000
    private val period: Long = 100

    private var info: SnackBarInfo? = null
    private var currentView: View? = null
    private var timer: Timer? = null
    private var currentMillis: Long = 0

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    fun show(message: String, onDismiss: (() -> Unit)? = null, onUndo: (() -> Unit)? = null) {
        dismiss()

        info = SnackBarInfo(message, onDismiss, onUndo)

        val view = LayoutInflater.from(context).inflate(R.layout.snackbar_layout, this)
        view?.setOnClickListener {  }

        view.findViewById<TextView>(R.id.contentText).text = message

        if (onUndo != null) {
            view.findViewById<View>(R.id.timerView).visibility = View.VISIBLE

            val btnUndo = view.findViewById<View>(R.id.btnUndo)
            btnUndo.visibility = View.VISIBLE
            btnUndo.setOnClickListener { undo() }

            view.findViewById<TextView>(R.id.undoTime).text = millisToSecond(snackbarLifeTime)

            val progressBar = view.findViewById<ProgressBar>(R.id.progressTimer)
            progressBar.max = snackbarLifeTime.toInt()
            progressBar.progress = snackbarLifeTime.toInt()
        }

        startNewTimer()

        currentView = view
    }

    fun dismiss() {
        info?.onDismiss?.invoke()
        clear()
    }

    fun undo() {
        info?.onUndo?.invoke()
        clear()
    }

    private fun clear() {
        timer?.cancel()
        timer = null

        info = null

        removeAllViews()
    }

    private fun millisToSecond(millis: Long): String {
        return Math.ceil(millis.toDouble() / 1000).toInt().toString()
    }

    private fun startNewTimer() {
        currentMillis = snackbarLifeTime

        timer = Timer()
        timer?.schedule(0, period) {
            handler.post {
                currentMillis -= period
                if (currentMillis <= 0) {
                    dismiss()
                } else if (info?.onUndo != null) {
                    currentView?.findViewById<TextView>(R.id.undoTime)?.text = millisToSecond(currentMillis)
                    currentView?.findViewById<ProgressBar>(R.id.progressTimer)?.progress = (snackbarLifeTime - currentMillis).toInt()
                }
            }
        }
    }

    private data class SnackBarInfo(val message: String, val onDismiss: (() -> Unit)? = null, val onUndo: (() -> Unit)? = null)
}