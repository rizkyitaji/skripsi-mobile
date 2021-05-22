@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.adapter

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences

class ProposalMHSAdapter(private var data: List<Skripsi>,
                         private var listener: (Skripsi) -> Unit)
    : RecyclerView.Adapter<ProposalMHSAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_recent_proposal, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var preferences: Preferences
        val dosen = view.findViewById<TextView>(R.id.tv_dosen)
        val judul = view.findViewById<TextView>(R.id.tv_judul)
        val action = view.findViewById<ImageView>(R.id.btn_action)
        val status = view.findViewById<TextView>(R.id.tv_status)

        fun bindItem(data: Skripsi, listener: (Skripsi) -> Unit, context: Context) {
            preferences = Preferences(context)
            dosen.text = data.pembimbing
            judul.text = data.judul
            status.text = data.status
            val id = data.id.toString()
            val nim = data.nim.toString()
            val nidn = data.nidn_fk.toString()

            val getStatus = status.text.toString()
            if (getStatus.equals("Ditolak")) {
                action.setImageResource(R.drawable.ic_baseline_delete)
                action.setOnClickListener {
                    if (isConnected(context)) {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Apakah Anda yakin ingin menghapus proposal ini?")
                                .setPositiveButton("Ya") { _, _ ->
                                    val ref = FirebaseDatabase.getInstance()
                                            .getReference("Proposal").child(id)
                                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            ref.removeValue()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                                        }
                                    })

                                    val db = FirebaseDatabase.getInstance()
                                            .getReference("Status").child(nim).child(id)
                                    db.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            db.removeValue()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                                        }
                                    })

                                    Toast.makeText(context, "Proposal sudah dihapus", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Tidak"){ dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (getStatus.equals("LULUS")){
                action.setImageResource(R.drawable.ic_keyboard_arrow_right)
            } else {
                action.setImageResource(R.drawable.ic_edit)
                action.setOnClickListener {
                    preferences.setValues("id", id)
                    preferences.setValues("nidn", data.nidn.toString())
                    if (getStatus.equals("Disetujui")){
                        preferences.setValues("action", "edit")
                    } else {
                        if (!nidn.equals("")) {
                            preferences.setValues("action", "change")
                        } else {
                            preferences.setValues("action", "modify")
                        }
                    }
                    listener(data)
                }
            }

            itemView.setOnClickListener {
                preferences.setValues("id", id)
                preferences.setValues("nim", data.nim.toString())
                preferences.setValues("prodi", data.prodi.toString())
                preferences.setValues("action", "detail")
                listener(data)
            }
        }

        private fun isConnected(context: Context): Boolean {
            val connectivity = context.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivity.activeNetworkInfo
            if (info != null) {
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
            return false
        }
    }
}