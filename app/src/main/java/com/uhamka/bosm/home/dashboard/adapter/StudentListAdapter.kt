package com.uhamka.bosm.home.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences

class StudentListAdapter(private var data: List<Skripsi>,
                         private var listener: (Skripsi) -> Unit)
    : RecyclerView.Adapter<StudentListAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_daftar_mhs, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tv_name)
        val title = view.findViewById<TextView>(R.id.tv_title)

        fun bindItem(data: Skripsi, listener: (Skripsi) -> Unit, context: Context) {
            val preferences = Preferences(context)
            name.text = data.name
            title.text = data.judul

            itemView.setOnClickListener {
                preferences.setValues("id", data.id.toString())
                preferences.setValues("nim", data.nim.toString())
                preferences.setValues("name", data.name.toString())
                preferences.setValues("prodi", data.prodi.toString())
                preferences.setValues("title", data.judul.toString())
                listener(data)
            }
        }
    }
}