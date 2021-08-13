package com.mw.beam.beamwallet.screens.asset_info

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.AssetManager
import com.mw.beam.beamwallet.core.helpers.PreferencesManager

import kotlinx.android.synthetic.main.fragment_asset_description.*

private const val ID = "id"

class AssetDescriptionFragment : Fragment() {
    private var assetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assetId = it.getInt(ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asset_description, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Int) =
                AssetDescriptionFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ID, id)
                    }
                }
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val asset = AssetManager.instance.getAsset(assetId)
        if (asset != null) {
            assetNameLabel.text = asset.unitName.toUpperCase()
            assetIcon.setImageResource(asset.image)

            shortDescLabel.text = asset.shortDesc
            longDescLabel.text = asset.longDesc
            smallNameLabel.text = asset.nthUnitName

            if (asset.site.isEmpty()) {
                siteLabel.visibility = View.GONE
            }
            else {
                siteLabel.setOnClickListener {
                    openLink(asset.site)
                }
            }

            if (asset.paper.isEmpty()) {
                descLabel.visibility = View.GONE
            }
            else {
                descLabel.setOnClickListener {
                    openLink(asset.paper)
                }
            }

            if (asset.shortDesc.isEmpty()) {
                shortDescLayout.visibility = View.GONE
            }

            if (asset.longDesc.isEmpty()) {
                longDescLayout.visibility = View.GONE
            }
        }
    }

    private fun openLink(link:String) {
        val allow = PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK)
        if (allow) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
        else{
            showAlert(getString(R.string.common_external_link_dialog_message),getString(R.string.open),{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            },getString(R.string.common_external_link_dialog_title),getString(R.string.cancel),{
            },requireContext(), true)
        }
    }


    @SuppressLint("InflateParams")
    fun showAlert(message: String, btnConfirmText: String, onConfirm: () -> Unit, title: String?, btnCancelText: String?, onCancel: () -> Unit, context: Context, cancelable: Boolean = true): AlertDialog? {

        val view = LayoutInflater.from(context).inflate(R.layout.common_alert_dialog, null)
        val alertTitle = view.findViewById<TextView>(R.id.title)
        val alertText = view.findViewById<TextView>(R.id.alertText)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        if (title.isNullOrBlank()) {
            alertTitle.visibility = View.GONE
        } else {
            alertTitle.text = title
        }

        alertText.text = message
        btnConfirm.text = btnConfirmText
        btnCancel.text = btnCancelText

        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(cancelable)
                .show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm.invoke()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
            onCancel.invoke()
        }

        return dialog
    }
}