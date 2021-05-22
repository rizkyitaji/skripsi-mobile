@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.adapter

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Pembimbing
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_proposal.*

class ProposalAdapter(private var data: List<Skripsi>,
                      private var listener: (Skripsi) -> Unit)
    : RecyclerView.Adapter<ProposalAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_proposal, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var preferences: Preferences
        private var dataList = ArrayList<Pembimbing>()
        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        val tv_dosen = view.findViewById<TextView>(R.id.tv_dosen)
        val tv_title = view.findViewById<TextView>(R.id.tv_title)
        val expand = view.findViewById<ImageView>(R.id.btn_expand)
        val content = view.findViewById<CardView>(R.id.cv_content)
        val expandableView = view.findViewById<ConstraintLayout>(R.id.action)
        val approve = view.findViewById<TextView>(R.id.btn_approve)
        val edit = view.findViewById<TextView>(R.id.btn_edit)

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
            tv_dosen.text = dosen
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

            val repository = FirebaseDatabase.getInstance()
                    .getReference("Repository").child(prodi).child(nim)

            val status = FirebaseDatabase.getInstance()
                    .getReference("Status").child(nim).child(id)

            approve.setOnClickListener {
                proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        proposal.child("status").setValue("Disetujui")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                skripsi.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        skripsi.removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                repository.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        repository.child("id").setValue(id)
                        repository.child("name").setValue(name)
                        repository.child("nim").setValue(nim)
                        repository.child("prodi").setValue(prodi)
                        repository.child("nidn").setValue(nidn)
                        repository.child("pembimbing").setValue(dosen)
                        repository.child("judul").setValue(title)
                        repository.child("desc").setValue(desc)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                status.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        status.child("status").setValue("Disetujui")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })

                Toast.makeText(context, "Proposal " + name + " sudah disetujui", Toast.LENGTH_LONG).show()
            }

            edit.setOnClickListener {
                val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
                val layoutInflater = LayoutInflater.from(context)
                val view = layoutInflater.inflate(R.layout.layout_edit_dosen, null)
                dialog.setContentView(view)
                dialog.show()

                val close = view.findViewById<ImageView>(R.id.iv_close)
                dialog.setCancelable(false)
                close.setOnClickListener {
                    dialog.dismiss()
                }

                val textName = view.findViewById<TextView>(R.id.tv_name)
                val textDosen = view.findViewById<TextView>(R.id.tv_dosen)
                val textTitle = view.findViewById<TextView>(R.id.tv_title)

                textName.text = name
                textTitle.text = title
                textDosen.text = dosen

                /*option dosen*/
                val btn_dosen = view.findViewById<ConstraintLayout>(R.id.btn_dosen)
                btn_dosen.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val v = inflater.inflate(R.layout.layout_dosen, null)
                    builder.setView(v)
                    val alert = builder.create()
                    alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    alert.show()

                    val rv = v.findViewById<RecyclerView>(R.id.rv_dosen)
                    rv.layoutManager = LinearLayoutManager(context)

                    val ref = FirebaseDatabase.getInstance().getReference("Pembimbing")
                            .child(prodi).orderByChild("name")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            dataList.clear()
                            for (snapshot in p0.children) {
                                dataList.add(snapshot.getValue(Pembimbing::class.java)!!)
                            }
                            rv.adapter = OptionDosenAdapter(dataList) {
                                alert.dismiss()
                                textDosen.text = preferences.getValues("nameD").toString()
                            }
                        }
                    })
                }

                val submit = view.findViewById<Button>(R.id.btn_submit)
                submit.setOnClickListener {
                    if (isConnected(context)) {
                        val getDosen = textDosen.text.toString()
                        val getNIDN = preferences.getValues("nidn").toString()

                        proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                proposal.child("status").setValue("Disetujui")
                                proposal.child("pembimbing").setValue(getDosen)
                                if (!getDosen.equals(dosen)) {
                                    proposal.child("nidn").setValue(getNIDN)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })

                        skripsi.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                skripsi.removeValue()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })

                        repository.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                repository.child("id").setValue(id)
                                repository.child("name").setValue(name)
                                repository.child("nim").setValue(nim)
                                repository.child("prodi").setValue(prodi)
                                repository.child("pembimbing").setValue(getDosen)
                                repository.child("judul").setValue(title)
                                repository.child("desc").setValue(desc)
                                if (!getDosen.equals(dosen)) {
                                    repository.child("nidn").setValue(getNIDN)
                                } else {
                                    repository.child("nidn").setValue(nidn)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })

                        status.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                status.child("status").setValue("Disetujui")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                            }
                        })

                        Toast.makeText(context, "Dosen pembimbing untuk " + name + " sudah diganti", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            itemView.setOnClickListener {
                preferences.setValues("nim", nim)
                preferences.setValues("prodi", prodi)
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