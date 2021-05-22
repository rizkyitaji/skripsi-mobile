package com.uhamka.bosm.home.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences

class ProposalDosenAdapter(private var data: List<Skripsi>,
                           private var listener: (Skripsi) -> Unit)
    : RecyclerView.Adapter<ProposalDosenAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_new_proposal, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var preferences: Preferences
        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        val tv_nim = view.findViewById<TextView>(R.id.tv_nim)
        val tv_title = view.findViewById<TextView>(R.id.tv_title)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)
        val content = view.findViewById<CardView>(R.id.cv_content)
        val expandableView = view.findViewById<ConstraintLayout>(R.id.action)
        val approve = view.findViewById<TextView>(R.id.btn_approve)
        val reject = view.findViewById<TextView>(R.id.btn_reject)

        fun bindItem(data: Skripsi, listener: (Skripsi) -> Unit, context: Context) {
            preferences = Preferences(context)
            val id = data.id.toString()
            val name = data.name.toString()
            val nim = data.nim.toString()
            val prodi = data.prodi.toString()
            val nidn = data.nidn.toString()
            val dosen = data.pembimbing.toString()
            val title = data.judul.toString()
            val desc = data.desc.toString()

            tv_name.text = name
            tv_nim.text = nim
            tv_title.text = title

            expand.setOnClickListener {
                if (expandableView.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(content, AutoTransition())
                    expandableView.visibility = View.VISIBLE
                    expand.setImageResource(R.drawable.ic_expand_less)
                } else {
                    TransitionManager.beginDelayedTransition(content, AutoTransition())
                    expandableView.visibility = View.GONE
                    expand.setImageResource(R.drawable.ic_expand_more)
                }
            }

            val proposal = FirebaseDatabase.getInstance()
                    .getReference("Proposal").child(id)

            val skripsi = FirebaseDatabase.getInstance()
                    .getReference("Skripsi").child(prodi).child(nim)

            val status = FirebaseDatabase.getInstance()
                    .getReference("Status").child(nim).child(id)

            approve.setOnClickListener {
                proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        proposal.child("nidn_fk").removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                skripsi.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        skripsi.child("id").setValue(id)
                        skripsi.child("name").setValue(name)
                        skripsi.child("nim").setValue(nim)
                        skripsi.child("prodi").setValue(prodi)
                        skripsi.child("nidn").setValue(nidn)
                        skripsi.child("pembimbing").setValue(dosen)
                        skripsi.child("judul").setValue(title)
                        skripsi.child("desc").setValue(desc)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
                Toast.makeText(context, "Proposal " + name + " sudah disetujui", Toast.LENGTH_LONG).show()
            }

            reject.setOnClickListener {
                proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        proposal.child("nidn_fk").removeValue()
                        proposal.child("status").setValue("Ditolak")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                status.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        status.child("status").setValue("Ditolak")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                Toast.makeText(context, "Proposal " + name + " sudah ditolak", Toast.LENGTH_LONG).show()
            }

            itemView.setOnClickListener {
                preferences.setValues("id", id)
                preferences.setValues("nim", nim)
                preferences.setValues("prodi", prodi)
                listener(data)
            }
        }
    }
}