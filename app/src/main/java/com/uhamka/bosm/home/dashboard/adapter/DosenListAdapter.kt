package com.uhamka.bosm.home.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Pembimbing
import com.uhamka.bosm.utils.Preferences

class DosenListAdapter(private var data: List<Pembimbing>,
                       private var listener: (Pembimbing) -> Unit)
    : RecyclerView.Adapter<DosenListAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_pembimbing, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var photo: String
        val dosen = view.findViewById<TextView>(R.id.tv_dosen)
        val expertise = view.findViewById<TextView>(R.id.tv_expertise)

        fun bindItem(data: Pembimbing, listener: (Pembimbing) -> Unit, context: Context) {
            val preferences = Preferences(context)
            val name = data.name.toString()
            val nidn = data.nidn.toString()
            dosen.text = name
            expertise.text = data.expertise

            val ref = FirebaseDatabase.getInstance()
                    .getReference("Users").child("Dosen").child(nidn)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    photo = snapshot.child("photo").value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, ""+error.message, Toast.LENGTH_SHORT).show()
                }
            })

            itemView.setOnClickListener {
                preferences.setValues("id", nidn)
                preferences.setValues("name", name)
                preferences.setValues("photo", photo)
                listener(data)
            }
        }
    }
}