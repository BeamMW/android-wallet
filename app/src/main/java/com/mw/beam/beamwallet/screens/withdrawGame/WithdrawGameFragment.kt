package com.mw.beam.beamwallet.screens.withdrawGame

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.loadingview.LoadingDialog
import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.convertToBeamString
import com.mw.beam.beamwallet.screens.app_activity.AppActivity
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URL
import java.math.BigInteger
import java.security.MessageDigest
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

class WithdrawGameFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_withdraw_game, container, false)
    }

    private var walletId = ""
    private val apiUrl = "http://78.47.156.39:2000"
    private var token = ""
    private var userId = ""
    private var amount = 0
    private var loadingDialog:LoadingDialog? = null

    override fun onStart() {
        super.onStart()

        if(amount == 0) {
            userId = AppActivity.withdrawUserId
            amount = AppActivity.withdrawAmount;

            val groth = AppActivity.withdrawAmount.toDouble() / 100000000.toDouble()

            availableSum.text = groth.convertToBeamString() + " BEAM"

            toolbar.title = "withdraw".toUpperCase()

            AppManager.instance.subOnBeamGameGenerated.subscribe {
                walletId = it
            }

            btnNext.setOnClickListener {
                generateToken()
            }

            AppManager.instance.createAddressForBeamGame()

            AppActivity.withdrawAmount = 0
        }
    }

    private fun showErrorAlert(text:String) {
        loadingDialog?.hide()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage(text)
        builder.setNegativeButton(android.R.string.ok) { _, _ ->

        }
        builder.show()
    }

    private fun generateToken() {
        if(!walletId.isNullOrEmpty()) {
            loadingDialog= LoadingDialog.get(activity!!).show()

            createToken()
        }
    }

    private fun sendMoney() {
        doAsync {
            val client = OkHttpClient()

            val md5 = "$amount" + userId + walletId
            val hash = md5.md5()

            val urlString =
                    "${apiUrl}/withdraw?user_id=${userId}&address=${walletId}" +
                            "&amount=${amount}" +
                            "&token=${token}&key=${hash}"

            val url = URL(urlString)

            val body = RequestBody.create(null, ByteArray(0))

            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            val response = client.newCall(request).execute()

            if (response.code() == 200) {
                uiThread {
                    loadingDialog?.hide()
                    activity?.onBackPressed()
                }
            }
            else {
                uiThread {
                    showErrorAlert("Something went wrong")
                }
            }
        }
    }

    private fun createToken() {
        doAsync {
            val client = OkHttpClient()

            val urlString =
                    "${apiUrl}/generate_token?user_id=${userId}&address=${walletId}&amount=${amount}"

            val url = URL(urlString)

            val body = RequestBody.create(null, ByteArray(0))

            val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

            val response = client.newCall(request).execute()

            if(response.code() == 200) {
                val responseBody = response.body()

                val mapperAll = ObjectMapper()
                val objData = mapperAll.readTree(responseBody?.byteStream())

                token = objData.get("token").textValue()

                sendMoney()
            }
            else {
                uiThread {
                    showErrorAlert("Something went wrong")
                }
            }
        }
    }
}