package com.uhamka.bosm.home.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Pembimbing
import com.uhamka.bosm.utils.Preferences

class OptionDosenAdapter(private var data: List<Pembimbing>,
                         private var listener: (Pembimbing) -> Unit)
    : RecyclerView.Adapter<OptionDosenAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_dosen, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dosen = view.findViewById<TextView>(R.id.tv_dosen)

        fun bindItem(data: Pembimbing, listener: (Pembimbing) -> Unit, context: Context) {
            val preferences = Preferences(context)
            dosen.text = data.name

            itemView.setOnClickListener {
                preferences.setValues("nidn", data.nidn.toString())
                preferences.setValues("nameD", data.name.toString())
                listener(data)
            }
        }
    }
}