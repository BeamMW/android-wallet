package com.mw.beam.beamwallet.screens.apps.list

import android.content.Context
import android.graphics.Color

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.mw.beam.beamwallet.R
import com.mw.beam.beamwallet.core.entities.DAOApp
import com.mw.beam.beamwallet.core.views.loadUrl


class AppsListAdapter(
    private val context: Context,
    private var apps: ArrayList<DAOApp>,
    private var clickListener: (DAOApp) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.cell_dao_app,
            viewGroup,
            false
        )
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as AppViewHolder).setDetails(apps[position])
        (viewHolder).mainLayout.setOnClickListener {
            clickListener.invoke(apps[position])
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    internal inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameLabel: TextView = itemView.findViewById(R.id.nameLabel)
        private val detailLabel: TextView = itemView.findViewById(R.id.detailLabel)
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)
        val mainLayout: LinearLayout = itemView.findViewById(R.id.mainLayout)

        fun setDetails(item: DAOApp) {
            nameLabel.text = item.name

            if (item.support == false) {
                detailLabel.setTextColor(context.getColor(R.color.category_red))
                detailLabel.text = context.getString(R.string.app_not_supported, item.api_version ?: "")
            }
            else {
                detailLabel.setTextColor(Color.WHITE)
                detailLabel.text = item.description
            }

            iconView.loadUrl(item.icon ?: "")
        }
    }
}